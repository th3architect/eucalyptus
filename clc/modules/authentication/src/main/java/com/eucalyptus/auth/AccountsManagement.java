package com.eucalyptus.auth;

import java.util.List;
import edu.ucsb.eucalyptus.msgs.ListUsersResponseType;
import edu.ucsb.eucalyptus.msgs.ListUsersType;
import edu.ucsb.eucalyptus.msgs.UserType;

public class AccountsManagement {
  
  public ListUsersResponseType listUsers( ListUsersType request ) {
    ListUsersResponseType response = request.getReply( );
    List<UserType> users = response.getUsers( );
    UserType user = new UserType( );
    user.setArn( "arn:aws:iam::123456789012:user/division_abc/subdivision_xyz/engineering/Andrew" );
    user.setPath( "/division_abc/subdivision_xyz/engineering/" );
    user.setUserId( "AID2MAB8DPLSRHEXAMPLE" );
    user.setUserName( "Andrew" );
    response.setIsTruncated( false );
    return response;
  }
  
}
