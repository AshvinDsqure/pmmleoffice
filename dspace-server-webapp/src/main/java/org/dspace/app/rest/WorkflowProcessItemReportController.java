/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.converter.ItemConverter;
import org.dspace.app.rest.converter.LoginCounterConverter;
import org.dspace.app.rest.enums.WorkFlowStatus;
import org.dspace.app.rest.model.CounterDTO;
import org.dspace.app.rest.model.ExcelDTO;
import org.dspace.app.rest.model.ItemRest;
import org.dspace.app.rest.model.LoginCounterRest;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.app.rest.utils.DateUtils;
import org.dspace.app.rest.utils.ExcelHelper;
import org.dspace.app.rest.utils.Utils;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.content.service.*;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller to upload bitstreams to a certain bundle, indicated by a uuid in the request
 * Usage: POST /api/core/bundles/{uuid}/bitstreams (with file and properties of file in request)
 * Example:
 * <pre>
 * {@code
 * curl https://<dspace.server.url>/api/core/bundles/d3599177-0408-403b-9f8d-d300edd79edb/bitstreams
 *  -XPOST -H 'Content-Type: multipart/form-data' \
 *  -H 'Authorization: Bearer eyJhbGciOiJI...' \
 *  -F "file=@Downloads/test.html" \
 *  -F 'properties={ "name": "test.html", "metadata": { "dc.description": [ { "value": "example file", "language": null,
 *          "authority": null, "confidence": -1, "place": 0 } ]}, "bundleName": "ORIGINAL" };type=application/json'
 * }
 * </pre>
 */
@RestController
@RequestMapping("/api/" + ItemRest.CATEGORY + "/" + ItemRest.PLURAL_NAME
        + "/report")
public class WorkflowProcessItemReportController {
    private static final Logger log = LogManager.getLogger();
    @Autowired
    protected Utils utils;
    @Autowired
    public ItemService itemService;
    @Autowired
    ConfigurationService configurationService;

    @Autowired
    WorkflowProcessNoteService workflowProcessNoteService;
    @Autowired
    private FeedbackService feedbackService;
    @Autowired
    LoginCounterService loginCounterService;

    @Autowired
    LoginCounterConverter loginCounterConverter;


    @Autowired
    ItemConverter itemConverter;
    @Autowired
    WorkFlowProcessMasterValueService workFlowProcessMasterValueService;


    /**
     * Method to upload a Bitstream to a Bundle with the given UUID in the URL. This will create a Bitstream with the
     * file provided in the request and attach this to the Item that matches the UUID in the URL.
     * This will only work for uploading one file, any extra files will silently be ignored
     *
     * @return The created BitstreamResource
     */
    @RequestMapping(method = RequestMethod.GET, value = "/getCountera")
    public Integer getContear(HttpServletRequest request) {
        Integer counter = 336889;
        try {
            // String baseurl = configurationService.getProperty("dspace.server.url");
            //  String counterFile = configurationService.getProperty("dspace.dir") +"/webapps/jspui/counter.txt";
            File f;
            f = new File("D://counter.txt");
            if (!f.exists()) {
                f.createNewFile();
            } else {
                ObjectInputStream inp = new ObjectInputStream(new FileInputStream(f));
                if (inp.available() > 0) {
                    counter = inp.readInt();
                }
                inp.close();
            }
            ObjectOutputStream oute = new ObjectOutputStream(new FileOutputStream(f));
            oute.writeInt(++counter);
            oute.flush();
            oute.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return counter;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getCounter")
    public Integer getConter(HttpServletRequest request) {
        Integer counter = 0;
        try {
            Context context = ContextUtil.obtainContext(request);
            counter = loginCounterService.countRows(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return counter;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getCounters")
    public List<CounterDTO> getCounters(HttpServletRequest request) throws SQLException {
        System.out.println("in counters");
        List<CounterDTO> rest = new ArrayList<>();
        try {
            Context context = ContextUtil.obtainContext(request);
            List<Object[]> list = loginCounterService.filter(context);
            for (int i = 0; i < list.size(); i++) {
                CounterDTO d = new CounterDTO();
                Object[] row = (Object[]) list.get(i);
                d.setMonth(row[0].toString() != null ? row[0].toString() : "-");
                d.setYear(row[1].toString() != null ? row[1].toString() : "-");
                d.setCount(row[2].toString() != null ? row[2].toString() : "-");
                System.out.println(row[0] + ", " + row[1]);
                rest.add(d);
            }
            return rest;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error counters");
            return null;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/sent")
    public String sent(HttpServletRequest request) throws SQLException {
        Context context = ContextUtil.obtainContext(request);
        String result = "";
        try {
            feedbackService.sendEmail(context, request, "ashvinmajethiya22@gmail.com", "no-reply@d2t.co", "Email ", "page");
            result = "success";
        } catch (IOException | MessagingException e) {
            e.printStackTrace();
            result = "faile";
            throw new RuntimeException(e.getMessage(), e);
        }
        return result;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/addCounter")
    public LoginCounterRest addConter(HttpServletRequest request) throws SQLException, AuthorizeException, ParseException {
        LoginCounterRest loginCounterRest = null;
        Context context = ContextUtil.obtainContext(request);
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();
        LoginCounter loginCounter = new LoginCounter();
        loginCounter.setLogindate(DateToSTRDDMMYYYHHMMSS(new Date()));
        loginCounter.setMonth("" + month);
        loginCounter.setYear("" + year);
        loginCounter.setUserid(context.getCurrentUser().getID());
        loginCounter = loginCounterService.create(context, loginCounter);
        loginCounterRest = loginCounterConverter.convert(loginCounter, utils.obtainProjection());
        context.commit();
        return loginCounterRest;
    }

    @PreAuthorize("hasPermission(#uuid, 'ITEAM', 'READ')")
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD}, value = "/getFileNumber")
    public Map<String, String> getFileNumber(HttpServletRequest request) throws Exception {
        String filenumber = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            EPerson currentuser = context.getCurrentUser();
            StringBuffer sb = new StringBuffer();
            WorkFlowProcessMasterValue department;
            if (currentuser != null) {
                department = workFlowProcessMasterValueService.find(context, context.getCurrentUser().getDepartment().getID());
                if (department.getPrimaryvalue() != null) {
                    sb.append(department.getSecondaryvalue());
                }
            }
            if (currentuser.getTablenumber() != null) {
                sb.append("/" + currentuser.getTablenumber());
            }
            sb.append("/File");
            sb.append("/" + DateUtils.getFinancialYear());
            int count = itemService.countTotal(context);
            count = count + 1;
            sb.append("/0000" + count);
            filenumber = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in getInwardNumber ");
        }
        Map<String, String> map = new HashMap<>();
        map.put("filenumber", filenumber);
        return map;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/downloadItemReport")
    public ResponseEntity<Resource> downloadItem(HttpServletRequest request,
                                                 @Parameter(value = "startdate", required = true) String startdate,
                                                 @Parameter(value = "enddate", required = true) String enddate) {
        try {
            Context context = ContextUtil.obtainContext(request);
            String filename = "ProductivityReport.xlsx";
            List<Item> list = itemService.getDataTwoDateRangeDownload(context, startdate, enddate);
            List<ExcelDTO> listDTo = list.stream().map(i -> {
                String title = itemService.getMetadataFirstValue(i, "dc", "title", null, null);
                String type = itemService.getMetadataFirstValue(i, "dc", "casetype", null, null);
                String issued = itemService.getMetadataFirstValue(i, "dc", "caseyear", null, null);
                type = (type != null) ? type : "-";
                title = (title != null) ? title : "-";
                issued = (issued != null) ? issued : "-";
                String caseDetail = type + "/" + title + "/" + issued;
                String uploaddate = itemService.getMetadataFirstValue(i, "dc", "date", "accessioned", null);
                uploaddate = (uploaddate != null) ? uploaddate : "-";
                String uploadedby = i.getSubmitter().getEmail();
                String hierarchy = i.getOwningCollection().getName();
                String email = (context.getCurrentUser() != null) ? context.getCurrentUser().getEmail() : "-";
                return new ExcelDTO(title, type, issued, caseDetail, uploaddate, uploadedby, hierarchy, email);
            }).collect(Collectors.toList());
            ByteArrayInputStream in = ExcelHelper.tutorialsToExcel(listDTo);
            InputStreamResource file = new InputStreamResource(in);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    @RequestMapping(method = RequestMethod.GET, value = "/margedDocumentanddownload")
    public ResponseEntity<Resource> margedDocumentanddownload(HttpServletRequest request,
                                                              @Parameter(value = "itemid", required = true) String itemid) {
        try {
            System.out.println("::::::::::::::::::::::::::::::mrgrd ::::::corrospondence::::::::and :::::notesheet");
            Context context = ContextUtil.obtainContext(request);
            String filename = "AllDoc.pdf";
            Item item = itemService.find(context, UUID.fromString(itemid));
            List<Bundle> bundles = item.getBundles("ORIGINAL");
            List<Bitstream> listofcorrospondenceBitstream = new ArrayList<>();
            List<Bitstream> listofnotesheetsBitsream = new ArrayList<>();
            if (bundles.size() != 0) {
                listofcorrospondenceBitstream = bundles.stream().findFirst().get().getBitstreams();
            }
            //corrospondence bitstream list
            listofcorrospondenceBitstream = listofcorrospondenceBitstream.stream().filter(f -> !f.getName().contains("Note#")).collect(Collectors.toList());
            //NoteSheet Doc List
            UUID statusid = WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context).get().getID();
            List<WorkflowProcessNote> listofnotesheets = workflowProcessNoteService.getDocumentByItemid(context, UUID.fromString(itemid), statusid, 0, 50);
            System.out.println("listofnotesheets::::::::::::::::" + listofnotesheets.size());
            for (WorkflowProcessNote workflowProcessNote : listofnotesheets) {
                if (workflowProcessNote.getWorkflowProcessReferenceDocs() != null) {
                    for (WorkflowProcessReferenceDoc workflowProcessReferenceDoc : workflowProcessNote.getWorkflowProcessReferenceDocs()) {
                        if (workflowProcessReferenceDoc.getDrafttype() != null && workflowProcessReferenceDoc.getDrafttype().getPrimaryvalue() != null && workflowProcessReferenceDoc.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Note")) {
                            if (workflowProcessReferenceDoc.getBitstream() != null) {
                                listofnotesheetsBitsream.add(workflowProcessReferenceDoc.getBitstream());
                            }
                        } else {
                        }
                    }
                }
            }
            System.out.println("::listofnotesheetsBitsream::size" + listofnotesheetsBitsream.size());
            System.out.println("::listofcorrospondenceBitstream::size" + listofcorrospondenceBitstream.size());
           /* ByteArrayInputStream in = ExcelHelper.tutorialsToExcel(listDTo);
            InputStreamResource file = new InputStreamResource(in);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);*/
            System.out.println("::::done::::mrgrd ::::::corrospondence::::::::and :::::notesheet");

        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return null;
    }

    public static Date DateToSTRDDMMYYYHHMMSS(Date date) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String s = formatter.format(date);
        return new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").parse(s);
    }
}
