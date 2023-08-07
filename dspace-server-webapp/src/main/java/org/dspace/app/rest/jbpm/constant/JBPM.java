/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.jbpm.constant;

public class JBPM {
   public static  final String CREATEPROCESS="/procdetails";
   public static  final String FORWARDPROCESS=CREATEPROCESS+"/forwardtask";
   public static  final String BACKWARDPROCESS=CREATEPROCESS+"/backwardtask";
   public static  final String HOLDPROCESS=CREATEPROCESS+"/suspendtask";
   public static  final String RESUMEPROCESS=CREATEPROCESS+"/resumetask";
   public static  final String REFERTASK=CREATEPROCESS+"/refertask";
   public static  final String RECEIVED=CREATEPROCESS+"/receiveditem";
   public static  final String GETTASKLIST=CREATEPROCESS+"/gettasklist";


}
