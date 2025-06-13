/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.itemimport;

import org.apache.commons.cli.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.*;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.eperson.service.EPersonService;
import org.dspace.handle.factory.HandleServiceFactory;
import org.dspace.handle.service.HandleService;
import org.dspace.scripts.DSpaceRunnable;
import org.dspace.storage.bitstore.factory.StorageServiceFactory;
import org.dspace.storage.bitstore.service.BitstreamStorageService;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Import items into DSpace. The conventional use is upload files by copying
 * them. DSpace writes the item's bitstreams into its assetstore. Metadata is
 * also loaded to the DSpace database.
 * <p>
 * A second use assumes the bitstream files already exist in a storage
 * resource accessible to DSpace. In this case the bitstreams are 'registered'.
 * That is, the metadata is loaded to the DSpace database and DSpace is given
 * the location of the file which is subsumed into DSpace.
 * <p>
 * The distinction is controlled by the format of lines in the 'contents' file.
 * See comments in processContentsFile() below.
 * <p>
 * Modified by David Little, UCSD Libraries 12/21/04 to
 * allow the registration of files (bitstreams) into DSpace.
 */
public class WorkflowMapping extends DSpaceRunnable<ItemImportScriptConfiguration> {

    private static boolean template = false;

    private static final CollectionService collectionService = ContentServiceFactory.getInstance().getCollectionService();
    private static final EPersonService epersonService = EPersonServiceFactory.getInstance().getEPersonService();

    private static final EpersonMappingService epersonServiceMapping = EPersonServiceFactory.getInstance().getEpersonMappingService();
    private static final EpersonToEpersonMappingService epersonToEpersonMappingService = EPersonServiceFactory.getInstance().epersonToEpersonMappingService();

    private static final WorkflowProcessEpersonService workflowProcessEpersonService = EPersonServiceFactory.getInstance().workflowProcessEpersonService();
    private static final WorkFlowProcessHistoryService workFlowProcessHistoryService = EPersonServiceFactory.getInstance().workFlowProcessHistoryService();

    private static final WorkflowProcessSenderDiaryEpersonService workflowProcessSenderDiaryEpersonService = EPersonServiceFactory.getInstance().workflowProcessSenderDiaryEpersonService();

    private static final WorkFlowProcessDraftDetailsService workFlowProcessDraftDetailsService = EPersonServiceFactory.getInstance().workFlowProcessDraftDetailsService();


    private static final HandleService handleService = HandleServiceFactory.getInstance().getHandleService();
    private static final CommunityService communityService = ContentServiceFactory.getInstance().getCommunityService();
    private static final BitstreamStorageService bitstreamStorageService = StorageServiceFactory.getInstance().getBitstreamStorageService();


    /**
     * Default constructor
     */
    private WorkflowMapping() {
    }

    public static void main(String[] argv) throws Exception {
        Instant start = Instant.now();
        System.out.println(":::::::::::::startTime ::::::::::" + start);
        int status = 0;
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("l", "update Migrated Data", true, "Limit of record");
        options.addOption("e", "update Migrated Data", true, "Limit of record");
        CommandLine line = parser.parse(options, argv);
        String eperson = "";
        int threadNumber = 7;
        int limitofrecord = -1;
        try {
            if (!line.hasOption('l')) {
                System.out.println("Error  with, Thread Count  must add");
                System.exit(1);
            }
            if (!line.hasOption('e')) { // eperson
                System.out.println("Error  with, Thread Count  must add");
                System.exit(1);
            }
            if (line.hasOption('l')) { // eperson
                limitofrecord = Integer.valueOf(line.getOptionValue('l'));
            }
            if (line.hasOption('e')) { // eperson
                eperson = line.getOptionValue('e');
            }
            Context cs = new Context();
            cs.setMode(Context.Mode.BATCH_EDIT);
            EPerson ePerson = epersonService.findByEmail(cs, eperson);
            cs.setCurrentUser(ePerson);
            cs.turnOffAuthorisationSystem();

//update WorkflowProcessEperson tble
            AtomicInteger successCount = new AtomicInteger(0);

            if (ePerson.getEmail().equalsIgnoreCase("support@d2t.co")) {

                List<WorkflowProcessEperson> bitstreams = workflowProcessEpersonService.getALLData(cs, limitofrecord);

                bitstreams.forEach(d -> {
                    try {
                      EpersonToEpersonMapping em= epersonToEpersonMappingService.findByEpersonbyEP(cs,d.getePerson().getID());
                        if (em!=null) {
                            d.setEpersontoepersonmapping(em);
                            workflowProcessEpersonService.update(cs, d);
                            successCount.incrementAndGet();
                            System.out.println("mapp done :::!!!"+d.getePerson().getEmail());
                        } else {
                            System.out.println("mapping not avalable:>>>>>>>>>>>>>>>>>>>>>>::"+d.getePerson().getEmail());
                        }
                    } catch (Exception e) {
                        System.out.println("error-->:::2:::" + e.getMessage());
                        // e.printStackTrace();
                    }
                });
            }
            else if (ePerson.getEmail().equalsIgnoreCase("akash22@gmail.com")) {
                System.out.println(":::::::::::::update History:::::::::::::::");
                List<WorkFlowProcessHistory> bitstreams = workFlowProcessHistoryService.getHistory(cs, limitofrecord);
                bitstreams.forEach(d -> {
                    try {
                        if(d.getSentto()!=null&&d.getSentto().getePerson()!=null&&d.getSentto().getePerson().getFullName()!=null){
                            d.setSenttoname(d.getSentto().getePerson().getFullName());
                        }
                       if(d.getWorkflowProcessEpeople()!=null&&d.getWorkflowProcessEpeople().getePerson()!=null&&d.getWorkflowProcessEpeople().getePerson().getFullName()!=null){
                           d.setSentbyname(d.getWorkflowProcessEpeople().getePerson().getFullName());
                           d.setIsupdate(true);
                           workFlowProcessHistoryService.update(cs,d);
                           successCount.incrementAndGet();
                           System.out.println("History update done :::!!!");
                       }else {
                           System.out.println(" not update d.WorkFlowProcessHistory()::::"+d.getID());
                       }
                    } catch (Exception e) {
                        System.out.println("error-->:::2:::" + e.getMessage());
                        // e.printStackTrace();
                    }
                });
            }
            else if (ePerson.getEmail().equalsIgnoreCase("ajay.x@pcmc.in")) {

                List<WorkflowProcessSenderDiaryEperson> bitstreams = workflowProcessSenderDiaryEpersonService.getALLData(cs, limitofrecord);

                bitstreams.forEach(d -> {
                    try {
                        EpersonToEpersonMapping em= epersonToEpersonMappingService.findByEpersonbyEP(cs,d.getePerson().getID());
                        if (em!=null) {
                            d.setEpersontoepersonmapping(em);
                            workflowProcessSenderDiaryEpersonService.update(cs, d);
                            successCount.incrementAndGet();
                            System.out.println("mapp done :::!!!"+d.getePerson().getEmail());
                        } else {
                            System.out.println("mapping not avalable:>>>>>>>>>>>>>>>>>>>>>>::"+d.getePerson().getEmail());
                        }
                    } catch (Exception e) {
                        System.out.println("error-->:::2:::" + e.getMessage());
                        // e.printStackTrace();
                    }
                });
            }
            else if(ePerson.getEmail().equalsIgnoreCase("vipul@gmail.com")){
             //update WorkflowProcessEperson tble done
                // add mapping ::::::::::::::::::::::::::::::::::::::::::::::::::::::::
                List<EPerson> bitstreams = epersonService.getAllNotNull(cs, limitofrecord);
                bitstreams.forEach(d -> {

                    try {

                        EpersonMapping epersonMapping=epersonMapping= epersonServiceMapping.findByOfficeAndDepartmentAndDesignationTableNo(cs, d.getOffice().getID(), d.getDepartment().getID(), d.getDesignation().getID(), d.getTablenumber());
                        if (epersonMapping != null) {
                            EpersonToEpersonMapping epersonMapping1 = new EpersonToEpersonMapping();
                            epersonMapping1.setIsactive(true);
                            epersonMapping1.setEpersonmapping(epersonMapping);
                            epersonMapping1.setEperson(d);
                            epersonMapping1.setIsdelete(false);
                            epersonToEpersonMappingService.create(cs, epersonMapping1);
                            d.setIsmap(true);
                            epersonService.update(cs, d);
                            System.out.println("done :::::::::1::::::" + d.getEmail());
                            successCount.incrementAndGet();

                        } else {
                            EpersonMapping epersonMapping1 = new EpersonMapping();
                            epersonMapping1.setOffice(d.getOffice());
                            epersonMapping1.setDepartment(d.getDepartment());
                            epersonMapping1.setDesignation(d.getDesignation());
                            Integer tbl = (d.getTablenumber() != null ? d.getTablenumber() : 0);
                            epersonMapping1.setTablenumber(tbl);
                            EpersonMapping epersonMapping2 = epersonServiceMapping.create(cs, epersonMapping1);
                            EpersonToEpersonMapping epersonMapping1q = new EpersonToEpersonMapping();
                            epersonMapping1q.setIsactive(true);
                            epersonMapping1q.setIsdelete(false);
                            epersonMapping1q.setEpersonmapping(epersonMapping2);
                            epersonMapping1q.setEperson(d);
                            epersonToEpersonMappingService.create(cs, epersonMapping1q);
                            d.setIsmap(true);
                            epersonService.update(cs, d);
                            successCount.incrementAndGet();
                            System.out.println("done :::::::::2::::::" + d.getEmail());
                        }

                    } catch (SQLException e) {
                        System.out.println("error-->:::1:::" + e.getMessage());
                        e.printStackTrace();
                    } catch (AuthorizeException e) {
                        System.out.println("error-->:::2:::" + e.getMessage());
                        e.printStackTrace();
                    }
                });

            }
            else{
                System.out.println("in Documentsignator ::::::::::::::::::::::>>>");
                List<WorkFlowProcessDraftDetails> bitstreams = workFlowProcessDraftDetailsService.getbyDocumentsignator(cs,limitofrecord);
                System.out.println("size:::"+bitstreams.size());
                bitstreams.forEach(d -> {
                    try {
                        EpersonToEpersonMapping em= epersonToEpersonMappingService.findByEpersonbyEP(cs,d.getDocumentsignator().getID());
                        if (em!=null) {
                            d.setEpersontoepersonmapping(em);
                            workFlowProcessDraftDetailsService.update(cs, d);
                            successCount.incrementAndGet();
                            System.out.println("mapp done  Documentsignator :::!!!"+d.getDocumentsignator().getEmail());
                        } else {
                            System.out.println("mapping getDocumentsignator not avalable:>>>>>>>>>>>>>>>>>>>>>>::"+d.getDocumentsignator().getEmail());
                        }
                    } catch (Exception e) {
                        System.out.println("error-->:::2:::" + e.getMessage());
                        // e.printStackTrace();
                    }
                });

            }

            System.out.println("Total successful records: " + successCount.get());
            System.out.println("done::::");
            cs.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public ItemImportScriptConfiguration getScriptConfiguration() {
        return null;
    }

    @Override
    public void setup() throws ParseException {

    }

    @Override
    public void internalRun() throws Exception {

    }
}