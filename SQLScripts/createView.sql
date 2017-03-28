use msdb;
if object_id('jobActivity') is not null
	drop view jobActivity;
go
/*
In der erstellten View wird nicht die [sysjobsteps] abgefragt, die listet die eigentlichen commands auf. Ein Job kann aus mehreren Schritten bestehen.
Ich denke für den Kalender uninteressant, für den Auftrag natürlich essentiell!

*/

create view jobActivity
as		
	with job_base1 as (
		select	job_id
				,max(run_date) over ( partition by job_id) as run_dateMax
				,run_date
				,run_time
				,run_duration
				,ROW_NUMBER() over (partition by job_id, run_date order by run_time desc) as rn
		from	sysjobhistory 
		where	step_id = 0
	)
	select	
			a.job_id,
		--	a.originating_server_id as originating_server_idJobs ,
			a.name as nameJobs,
			a.enabled as eneabledJobs,
			a.description,
			a.start_step_id,
			a.category_id as category_idJobs,
		--	a.owner_sid as owner_sidJobs ,
			a.notify_level_eventlog,
			a.notify_level_email,
			a.notify_level_netsend,
			a.notify_level_page,
			a.notify_email_operator_id,
			a.notify_netsend_operator_id,
			a.delete_level,
			a.date_created as date_createJobs,
			a.date_modified as date_modifiedJobs,
			a.version_number as version_numberJobs,
			--b.schedule_id,
			b.next_run_date,
			b.next_run_time,
			c.*,
			d.run_date,
			d.run_time,
			d.run_duration
	from	sysjobs a
		join	sysjobschedules b	on a.job_id			= b.job_id
		join	sysschedules c		on b.schedule_id	= c.schedule_id
		left	join job_base1 d	on a.job_id			= d.job_id
					and					d.run_dateMax	= d.run_date 
					and					d.rn = 1
go

-- select * from jobActivity;

/*
-- relevante Tabellen/Views die Informationen über die Aufträge bereithalten

select * from sysjobschedules 
select * from sysjobactivity
select * from sysjobhistory
select * from sysjobservers
select * from sysjobsteps
select * from sysjobstepslogs
select * from sysjobs
-- zeigt Einzelheiten der Zeitpläne
select * from sysschedules

*/
