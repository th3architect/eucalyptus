/*
 * Copyright (c) 2009  Eucalyptus Systems, Inc.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, only version 3 of the License.
 *
 *
 *  This file is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Please contact Eucalyptus Systems, Inc., 130 Castilian
 *  Dr., Goleta, CA 93101 USA or visit <http://www.eucalyptus.com/licenses/>
 *  if you need additional information or have any questions.
 *
 *  This file may incorporate work covered under the following copyright and
 *  permission notice:
 *
 *    Software License Agreement (BSD License)
 *
 *    Copyright (c) 2008, Regents of the University of California
 *    All rights reserved.
 *
 *    Redistribution and use of this software in source and binary forms, with
 *    or without modification, are permitted provided that the following
 *    conditions are met:
 *
 *      Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *
 *      Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 *    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 *    IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 *    TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 *    PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 *    OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *    EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *    PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 *    PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *    NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. USERS OF
 *    THIS SOFTWARE ACKNOWLEDGE THE POSSIBLE PRESENCE OF OTHER OPEN SOURCE
 *    LICENSED MATERIAL, COPYRIGHTED MATERIAL OR PATENTED MATERIAL IN THIS
 *    SOFTWARE, AND IF ANY SUCH MATERIAL IS DISCOVERED THE PARTY DISCOVERING
 *    IT MAY INFORM DR. RICH WOLSKI AT THE UNIVERSITY OF CALIFORNIA, SANTA
 *    BARBARA WHO WILL THEN ASCERTAIN THE MOST APPROPRIATE REMEDY, WHICH IN
 *    THE REGENTS' DISCRETION MAY INCLUDE, WITHOUT LIMITATION, REPLACEMENT
 *    OF THE CODE SO IDENTIFIED, LICENSING OF THE CODE SO IDENTIFIED, OR
 *    WITHDRAWAL OF THE CODE CAPABILITY TO THE EXTENT NEEDED TO COMPLY WITH
 *    ANY SUCH LICENSES OR RIGHTS.
 *******************************************************************************/
package com.eucalyptus.reporting.event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import com.eucalyptus.util.OwnerFullName;

/**
 * AddressEvent is an event indicating address resource activity in the CLC.
 *
 * <p>The address will be in a canonical format to allow use as a natural
 * identifier for the resource.</p>
 *
 * <p>Events will be fired when an address is allocated, released, associated
 * or disassociated and is triggered (possibly indirectly) by user
 * activity.</p>
 */
public class AddressEvent implements Event {

  public enum AddressAction { ALLOCATE, RELEASE, ASSOCIATE, DISASSOCIATE }

  private final String address;
  private final String userId;
  private final String userName;
  private final String accountId;
  private final String accountName;
  private final AddressAction action;

  public static AddressEvent with( final String address,
                                   final OwnerFullName owner,
                                   final AddressAction action ) {
    return new AddressEvent(
        address,
        owner.getUserId(),
        owner.getUserName(),
        owner.getAccountNumber(),
        owner.getAccountName(),
        action
    );
  }

  private AddressEvent( final String address,
                        final String userId,
                        final String userName,
                        final String accountId,
                        final String accountName,
                        final AddressAction action ) {
    assertThat( address, notNullValue() );
    assertThat( userId, notNullValue() );
    assertThat( userName, notNullValue() );
    assertThat( accountId, notNullValue() );
    assertThat( accountName, notNullValue() );
    assertThat( action, notNullValue() );

    this.address = address;
    this.userId = userId;
    this.userName = userName;
    this.accountId = accountId;
    this.accountName = accountName;
    this.action = action;
  }

  public String getAddress() {
    return address;
  }

  public String getUserId() {
    return userId;
  }

  public String getUserName() {
    return userName;
  }

  public String getAccountId() {
    return accountId;
  }

  public String getAccountName() {
    return accountName;
  }

  public AddressAction getAction() {
    return action;
  }

  @Override
  public boolean requiresReliableTransmission() {
    return false;
  }

  public String toString() {
    return String.format(
        "[address:%s,userId:%s,accountId:%s,action:%s]",
        address, userId, accountId, action );
  }
}
