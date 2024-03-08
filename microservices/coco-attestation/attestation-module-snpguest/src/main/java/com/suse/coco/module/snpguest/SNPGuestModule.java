/*
 * Copyright (c) 2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.coco.module.snpguest;

import com.suse.coco.modules.AttestationModule;
import com.suse.coco.modules.AttestationWorker;

import java.util.List;

public class SNPGuestModule implements AttestationModule {

    @Override
    public String getName() {
        return "snpguest";
    }

    @Override
    public int getSupportedType() {
        return 1;
    }

    @Override
    public AttestationWorker getWorker() {
        return new SNPGuestWorker();
    }

    @Override
    public List<String> getAdditionalMappers() {
        return List.of("mappers/snpguest.xml");
    }
}
