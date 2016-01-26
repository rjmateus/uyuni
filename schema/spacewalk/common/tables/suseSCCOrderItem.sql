--
-- Copyright (c) 2015 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--

CREATE TABLE suseSCCOrderItem
(
    id             NUMBER NOT NULL
                     CONSTRAINT suse_sccorder_id_pk
                     PRIMARY KEY,
    scc_id         NUMBER NOT NULL,
    credentials_id NUMBER
                       CONSTRAINT suse_sccorder_credsid_fk
                       REFERENCES suseCredentials (id)
                       ON DELETE CASCADE,
    sku            VARCHAR2(256),
    start_date     timestamp with local time zone,
    end_date       timestamp with local time zone,
    quantity       NUMBER DEFAULT(0),
    subscription_id NUMBER NOT NULL,
    created        timestamp with local time zone
                       DEFAULT (current_timestamp) NOT NULL,
    modified       timestamp with local time zone
                       DEFAULT (current_timestamp) NOT NULL
)
ENABLE ROW MOVEMENT
;

CREATE UNIQUE INDEX suse_sccorder_sccid_uq
    ON suseSCCOrderItem (scc_id);

CREATE INDEX suse_sccorder_subid_idx
    ON suseSCCOrderItem (subscription_id);

CREATE SEQUENCE suse_sccorder_id_seq;
