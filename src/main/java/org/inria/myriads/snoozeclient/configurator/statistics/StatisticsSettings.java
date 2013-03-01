/**
 * Copyright (C) 2010-2013 Eugen Feller, INRIA <eugen.feller@inria.fr>
 *
 * This file is part of Snooze, a scalable, autonomic, and
 * energy-aware virtual machine (VM) management framework.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 */
package org.inria.myriads.snoozeclient.configurator.statistics;

/**
 * Statistics settings.
 * 
 * @author Eugen Feller
 */
public final class StatisticsSettings 
{    
    /** Defines if statistics is enabled. */
    private Boolean isEnabled_;
    
    /** Output. */
    private StatisticsOutput output_;
    
    /** Constructor. */
    public StatisticsSettings()
    {
        output_ = new StatisticsOutput();
    }
    
    /**
     * Sets the statistics flag.
     * 
     * @param isEnabled     true if enabled, false otherwise
     */
    public void setEnabled(Boolean isEnabled) 
    {
        isEnabled_ = isEnabled;
    }

    /**
     * Returns the statistics status.
     * 
     * @return  true if enabled, false otherwise
     */
    public boolean isEnabled()
    {
        return isEnabled_;
    }
    
    /**
     * Returns the output parameters.
     * 
     * @return  The output parameters
     */
    public StatisticsOutput getOutput()
    {
        return output_;
    }
}
