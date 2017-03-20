use msdb;
if object_id('jobActivity') is not null
	drop view jobActivity;
go
/*
In der erstellten View wird nicht die [sysjobsteps] abgefragt, die listet die eigentlichen commands auf. Ein Job kann aus mehreren Schritten bestehen.
Ich denke f�r den Kalender uninteressant, f�r den Auftrag nat�rlich essentiell!

*/

create view jobActivity
as		select	a.job_id,
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
				c.*
		from	sysjobs a
		join	sysjobschedules b on a.job_id = b.job_id
		join	sysschedules c on b.schedule_id = c.schedule_id;
go

-- select * from jobActivity;


/*
-- relevante Tabellen/Views die Informationen �ber die Auftr�ge bereithalten

select * from sysjobschedules 
select * from sysjobactivity
select * from sysjobhistory
select * from sysjobservers
select * from sysjobsteps
select * from sysjobstepslogs
select * from sysjobs
-- zeigt Einzelheiten der Zeitpl�ne
select * from sysschedules

*/