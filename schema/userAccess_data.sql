

insert into suseWebEndpoint values (sequence_nextval('suseWebEndpoint_id_seq'), 'AdminViewsController', '/manager/admin/setup/payg', 'GET', 'W', true, true);
insert into suseWebEndpoint values (sequence_nextval('suseWebEndpoint_id_seq'), 'AdminViewsController', '/manager/admin/setup/payg/:id', 'GET', 'W', true, true);

insert into suseWebEndpoint values (sequence_nextval('suseWebEndpoint_id_seq'), 'AdminViewsController', '/manager/systems/list/all', 'GET', 'W', true, true);
insert into suseWebEndpoint values (sequence_nextval('suseWebEndpoint_id_seq'), 'AdminViewsController', '/manager/api/systems/list/all', 'GET', 'W', true, true);
insert into suseWebEndpoint values (sequence_nextval('suseWebEndpoint_id_seq'), 'AdminViewsController', '/manager/api/sets/:label', 'GET', 'W', true, true);
insert into suseWebEndpoint values (sequence_nextval('suseWebEndpoint_id_seq'), 'AdminViewsController', '/manager/api/sets/:label', 'POST', 'W', true, true);
insert into suseWebEndpoint values (sequence_nextval('suseWebEndpoint_id_seq'), 'AdminViewsController', '/manager/api/sets/:label/clear', 'GET', 'W', true, true);

insert into suseWebEndpoint values (sequence_nextval('suseWebEndpoint_id_seq'), 'YourRhn.do', '/YourRhn.do', 'GET', 'W', true, true);
insert into suseWebEndpoint values (sequence_nextval('suseWebEndpoint_id_seq'), 'systems/details/Overview.do', '/systems/details/Overview.do', 'GET', 'W', true, true);



insert into suseUserWebEndpoint values (2,(select id from suseWebEndpoint where endpoint ='/manager/admin/setup/payg' ));
insert into suseUserWebEndpoint values (2,(select id from suseWebEndpoint where endpoint ='/manager/admin/setup/payg/:id' ));

-- USER assign
insert into suseUserWebEndpoint (user_id, web_endpoint_id)
select 2, id from suseWebEndpoint where class_method like 'UserHandler%';

insert into suseUserWebEndpoint (
select 2, id from suseWebEndpoint where class_method = 'AdminViewsController' and scope = 'W');

insert into suseUserWebEndpoint (
select 2, id from suseWebEndpoint where class_method = 'YourRhn.do' and scope = 'W');

insert into suseUserWebEndpoint (
select 2, id from suseWebEndpoint where class_method = 'systems/details/Overview.do' and scope = 'W');

