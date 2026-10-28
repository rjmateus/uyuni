--
-- Copyright (c) 2026 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

create table
rhnChannelFilter
(
        id              BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
        channel_id      NUMERIC references rhnChannel (id) on delete cascade,
        sort_order	    NUMERIC NOT NULL,
        flag            VARCHAR(1) NOT NULL
                        check (flag in ('+','-')),
        filter          VARCHAR(4000) NOT NULL,
        created         TIMESTAMPTZ default(current_timestamp) NOT NULL,
        modified        TIMESTAMPTZ default(current_timestamp) NOT NULL
);


CREATE UNIQUE INDEX rhn_cf_cid_so_uq
    ON rhnChannelFilter (channel_id, sort_order);

