CREATE TABLE suseWebEndpoint
(
    id          NUMERIC NOT NULL
        CONSTRAINT suseWebEndpoint_pk PRIMARY KEY,
    namespace       VARCHAR(255) NOT NULL,
    class_name      VARCHAR(255),
    endpoint    VARCHAR(255) NOT NULL,
    scope       CHAR(1)    NOT NULL
        CONSTRAINT suseWebEndpoint_scope_ck
            CHECK (scope in ('A', 'W')),
    created              TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL,
    modified             TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL

);

CREATE SEQUENCE suseWebEndpoint_id_seq;

CREATE UNIQUE INDEX suseWebEndpoint_endpoint_uq
    ON suseWebEndpoint (endpoint);



CREATE TABLE suseUserWebEndpoint
(
    user_id   NUMERIC NOT NULL
                    CONSTRAINT suse_user_web_endpoint_user_fk
                        REFERENCES web_contact (id)
                        ON DELETE CASCADE,
    web_endpoint_id  NUMERIC NOT NULL
                    CONSTRAINT suse_user_web_endpoint_endpoint_fk
                        REFERENCES suseWebEndpoint (id)
                        ON DELETE CASCADE
);



CREATE TABLE suseUserGroup
(
    id               NUMERIC NOT NULL
                         CONSTRAINT suse_user_group_pk PRIMARY KEY,
    name             VARCHAR(64) NOT NULL,
    description      VARCHAR(1024) NOT NULL,

    system_group          CHAR(1) DEFAULT ('N') NOT NULL,
    org_id           NUMERIC
                         CONSTRAINT rhn_user_group_org_fk
                             REFERENCES web_customer (id)
                             ON DELETE CASCADE,
    created          TIMESTAMPTZ
                         DEFAULT (current_timestamp) NOT NULL,
    modified         TIMESTAMPTZ
                         DEFAULT (current_timestamp) NOT NULL
);
-- maybe we can have the system groups as not assign to any organization
-- then for each organization user can define new groups
CREATE UNIQUE INDEX suse_ug_oid_name_uq
    ON suseUserGroup (org_id, name);




-----
insert into suseWebEndpoint values (sequence_nextval('suseWebEndpoint_id_seq'), 'user', 'UserHandler', 'UserHandler.getDetails', 'A');
insert into suseWebEndpoint values (sequence_nextval('suseWebEndpoint_id_seq'), 'user', 'UserHandler', 'UserHandler.listUsers', 'A');
insert into suseWebEndpoint values (sequence_nextval('suseWebEndpoint_id_seq'), 'user', 'UserHandler', 'UserHandler.listRoles', 'A');


insert into suseUserWebEndpoint values (1, (select id from suseWebEndpoint where endpoint ='UserHandler.getDetails' ));
insert into suseUserWebEndpoint values (2,(select id from suseWebEndpoint where endpoint ='UserHandler.getDetails' ));
insert into suseUserWebEndpoint values (2,(select id from suseWebEndpoint where endpoint ='UserHandler.listUsers' ));
insert into suseUserWebEndpoint values (2,(select id from suseWebEndpoint where endpoint ='UserHandler.listRoles' ));


insert into suseUserWebEndpoint values (2,(select id from suseWebEndpoint where endpoint ='SystemHandler.createSystemProfile' ));

insert into suseUserWebEndpoint (user_id, web_endpoint_id)
select 2, id from suseWebEndpoint where namespace = 'user';

-------------
insert into suseWebEndpoint values (sequence_nextval('suseWebEndpoint_id_seq'), 'ui_payg', 'AdminViewsController', '/manager/admin/setup/payg', 'W');
insert into suseWebEndpoint values (sequence_nextval('suseWebEndpoint_id_seq'), 'ui_payg', 'AdminViewsController', '/manager/admin/setup/payg/:id', 'W');

insert into suseWebEndpoint values (sequence_nextval('suseWebEndpoint_id_seq'), 'systems_list', 'AdminViewsController', '/manager/systems/list/all', 'W');
insert into suseWebEndpoint values (sequence_nextval('suseWebEndpoint_id_seq'), 'systems_list', 'AdminViewsController', '/manager/api/systems/list/all', 'W');
insert into suseWebEndpoint values (sequence_nextval('suseWebEndpoint_id_seq'), 'systems_list', 'AdminViewsController', '/manager/api/sets/:label', 'W');
insert into suseWebEndpoint values (sequence_nextval('suseWebEndpoint_id_seq'), 'systems_list', 'AdminViewsController', '/manager/api/sets/:label/clear', 'W');

insert into suseUserWebEndpoint values (2,(select id from suseWebEndpoint where endpoint ='/manager/admin/setup/payg' ));
insert into suseUserWebEndpoint values (2,(select id from suseWebEndpoint where endpoint ='/manager/admin/setup/payg/:id' ));


insert into suseUserWebEndpoint (
select 2, id from suseWebEndpoint where namespace = 'systems_list' and scope = 'W');

