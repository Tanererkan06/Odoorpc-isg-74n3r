/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p/>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p/>
 * Created on 2/1/15 2:25 PM
 */
package com.odoo.addons.Risk.providers;

import com.odoo.addons.Risk.models.RiskAsessment_method_model;
import com.odoo.core.orm.provider.BaseModelProvider;

public class RiskAsessment_method_SyncProvider extends BaseModelProvider {
    public static final String TAG = RiskAsessment_method_SyncProvider.class.getSimpleName();


    @Override
    public String authority() {
        return RiskAsessment_method_model.AUTHORITY;
    }
}
