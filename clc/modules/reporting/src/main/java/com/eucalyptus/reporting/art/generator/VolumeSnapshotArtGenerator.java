/*************************************************************************
 * Copyright 2009-2012 Eucalyptus Systems, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * Please contact Eucalyptus Systems, Inc., 6755 Hollister Ave., Goleta
 * CA 93117, USA or visit http://www.eucalyptus.com/licenses/ if you need
 * additional information or have any questions.
 *
 * This file may incorporate work covered under the following copyright
 * and permission notice:
 *
 *   Software License Agreement (BSD License)
 *
 *   Copyright (c) 2008, Regents of the University of California
 *   All rights reserved.
 *
 *   Redistribution and use of this software in source and binary forms,
 *   with or without modification, are permitted provided that the
 *   following conditions are met:
 *
 *     Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *     Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer
 *     in the documentation and/or other materials provided with the
 *     distribution.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 *   FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 *   COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 *   INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *   BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *   CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *   LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 *   ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *   POSSIBILITY OF SUCH DAMAGE. USERS OF THIS SOFTWARE ACKNOWLEDGE
 *   THE POSSIBLE PRESENCE OF OTHER OPEN SOURCE LICENSED MATERIAL,
 *   COPYRIGHTED MATERIAL OR PATENTED MATERIAL IN THIS SOFTWARE,
 *   AND IF ANY SUCH MATERIAL IS DISCOVERED THE PARTY DISCOVERING
 *   IT MAY INFORM DR. RICH WOLSKI AT THE UNIVERSITY OF CALIFORNIA,
 *   SANTA BARBARA WHO WILL THEN ASCERTAIN THE MOST APPROPRIATE REMEDY,
 *   WHICH IN THE REGENTS' DISCRETION MAY INCLUDE, WITHOUT LIMITATION,
 *   REPLACEMENT OF THE CODE SO IDENTIFIED, LICENSING OF THE CODE SO
 *   IDENTIFIED, OR WITHDRAWAL OF THE CODE CAPABILITY TO THE EXTENT
 *   NEEDED TO COMPLY WITH ANY SUCH LICENSES OR RIGHTS.
 ************************************************************************/
package com.eucalyptus.reporting.art.generator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.eucalyptus.entities.EntityWrapper;
import com.eucalyptus.reporting.art.entity.AccountArtEntity;
import com.eucalyptus.reporting.art.entity.AvailabilityZoneArtEntity;
import com.eucalyptus.reporting.art.entity.ReportArtEntity;
import com.eucalyptus.reporting.art.entity.UserArtEntity;
import com.eucalyptus.reporting.art.entity.VolumeArtEntity;
import com.eucalyptus.reporting.art.entity.VolumeSnapshotUsageArtEntity;
import com.eucalyptus.reporting.art.util.DurationCalculator;
import com.eucalyptus.reporting.domain.ReportingAccount;
import com.eucalyptus.reporting.domain.ReportingAccountDao;
import com.eucalyptus.reporting.domain.ReportingUser;
import com.eucalyptus.reporting.domain.ReportingUserDao;
import com.eucalyptus.reporting.event_store.ReportingVolumeCreateEvent;
import com.eucalyptus.reporting.event_store.ReportingVolumeSnapshotCreateEvent;

public class VolumeSnapshotArtGenerator
	implements ArtGenerator
{
    private static Logger log = Logger.getLogger( VolumeSnapshotArtGenerator.class );

    public VolumeSnapshotArtGenerator()
	{
		
	}
	
	public ReportArtEntity generateReportArt(ReportArtEntity report)
	{
		log.debug("GENERATING REPORT ART");
		EntityWrapper wrapper = EntityWrapper.get( ReportingVolumeCreateEvent.class );

		/* Create super-tree of availZones, accounts, users, and volumes;
		 * and create a Map of the volume nodes at the bottom with start times.
		 */
		Iterator iter = wrapper.scanWithNativeQuery( "scanVolumeCreateEvents" );
		Map<String,VolumeArtEntity> volumeEntities = new HashMap<String,VolumeArtEntity>();
		while (iter.hasNext()) {
			ReportingVolumeCreateEvent createEvent = (ReportingVolumeCreateEvent) iter.next();
			if (! report.getZones().containsKey(createEvent.getAvailabilityZone())) {
				report.getZones().put(createEvent.getAvailabilityZone(), new AvailabilityZoneArtEntity());
			}
			AvailabilityZoneArtEntity zone = report.getZones().get(createEvent.getAvailabilityZone());
			
			ReportingUser reportingUser = ReportingUserDao.getInstance().getReportingUser(createEvent.getUserId());
			if (reportingUser==null) {
				log.error("No user corresponding to event:" + createEvent.getUserId());
			}
			ReportingAccount reportingAccount = ReportingAccountDao.getInstance().getReportingAccount(reportingUser.getAccountId());
			if (reportingAccount==null) {
				log.error("No account corresponding to user:" + reportingUser.getAccountId());
			}
			if (! zone.getAccounts().containsKey(reportingAccount.getName())) {
				zone.getAccounts().put(reportingAccount.getName(), new AccountArtEntity());
			}
			AccountArtEntity account = zone.getAccounts().get(reportingAccount.getName());
			if (! account.getUsers().containsKey(reportingUser.getName())) {
				account.getUsers().put(reportingUser.getName(), new UserArtEntity());
			}
			UserArtEntity user = account.getUsers().get(reportingUser.getName());
			VolumeArtEntity volume = new VolumeArtEntity(createEvent.getVolumeId());
			user.getVolumes().put(createEvent.getUuid(), volume);
			volumeEntities.put(createEvent.getUuid(), volume);
		}
		

		iter = wrapper.scanWithNativeQuery( "scanVolumeSnapshotCreateEvents" );
		Map<String, VolumeSnapshotUsageArtEntity> snapshotEntities = new HashMap<String, VolumeSnapshotUsageArtEntity>();
		Map<String, Long> snapshotStartTimes = new HashMap<String, Long>();
		while (iter.hasNext()) {
			ReportingVolumeSnapshotCreateEvent createEvent = (ReportingVolumeSnapshotCreateEvent) iter.next();
			if (createEvent.getTimestampMs() > report.getEndMs()) continue; //not included in this report
			VolumeSnapshotUsageArtEntity usage = new VolumeSnapshotUsageArtEntity();
			usage.setSizeGB(createEvent.getSizeGB());
			usage.setSnapshotNum(1);
			/* Default sizeGB is remainder of report * GB. This will be overwritten later if there's
			 * a corresponding delete event before the report end, later.
			 */
			usage.setGBSecs(createEvent.getSizeGB() * DurationCalculator.boundDuration(report.getBeginMs(),
					report.getEndMs(), createEvent.getTimestampMs())/1000);
			VolumeArtEntity volume = volumeEntities.get(createEvent.getVolumeUuid());
			volume.getSnapshotUsage().put(createEvent.getVolumeSnapshotId(), usage);
			snapshotEntities.put(createEvent.getUuid(), usage);
			snapshotStartTimes.put(createEvent.getUuid(), createEvent.getTimestampMs());
	}
		
		iter = wrapper.scanWithNativeQuery( "scanVolumeSnapshotDeleteEvents" );
		while (iter.hasNext()) {
			ReportingVolumeSnapshotCreateEvent deleteEvent = (ReportingVolumeSnapshotCreateEvent) iter.next();
			if (snapshotEntities.containsKey(deleteEvent.getUuid())) {
				VolumeSnapshotUsageArtEntity snap = snapshotEntities.get(deleteEvent.getUuid());
				long startTimeMs = snapshotStartTimes.get(deleteEvent.getUuid()).longValue();
				long duration = DurationCalculator.boundDuration(report.getBeginMs(), report.getEndMs(),
						startTimeMs, deleteEvent.getTimestampMs())/1000;
				snap.setGBSecs(snap.getSizeGB() * duration);
			}
		}
		
		/* Perform totals and summations for user, account, and zone
		 */
		for (String zoneName : report.getZones().keySet()) {
			AvailabilityZoneArtEntity zone = report.getZones().get(zoneName);
			for (String accountName : zone.getAccounts().keySet()) {
				AccountArtEntity account = zone.getAccounts().get(accountName);
				for (String userName : account.getUsers().keySet()) {
					UserArtEntity user = account.getUsers().get(userName);
					for (String volumeUuid : user.getVolumes().keySet()) {
						VolumeArtEntity volume = user.getVolumes().get(volumeUuid);
						for (String snapId: volume.getSnapshotUsage().keySet()) {
							VolumeSnapshotUsageArtEntity snap = volume.getSnapshotUsage().get(snapId);
							updateUsageTotals(volume.getSnapshotTotals(), snap);							
							updateUsageTotals(user.getUsageTotals().getSnapshotTotals(), snap);
							updateUsageTotals(account.getUsageTotals().getSnapshotTotals(), snap);
							updateUsageTotals(zone.getUsageTotals().getSnapshotTotals(), snap);
							
						}
					}
				}
			}
		}


		return report;
	}
	
	private static void updateUsageTotals(VolumeSnapshotUsageArtEntity totalEntity,
			VolumeSnapshotUsageArtEntity newEntity)
	{
		
		totalEntity.setSnapshotNum(newEntity.getSnapshotNum()+totalEntity.getSnapshotNum());
		totalEntity.setGBSecs(totalEntity.getGBSecs()+newEntity.getGBSecs());
		totalEntity.setSizeGB(plus(totalEntity.getSizeGB(), newEntity.getSizeGB()));

	}

	/**
	 * Addition with the peculiar semantics for null we need here
	 */
	private static Long plus(Long added, Long defaultVal)
	{
		if (added==null) {
			return defaultVal;
		} else if (defaultVal==null) {
			return added;
		} else {
			return (added.longValue() + defaultVal.longValue());
		}
		
	}
	
}
