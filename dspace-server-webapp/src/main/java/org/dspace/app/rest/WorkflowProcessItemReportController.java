/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.converter.ItemConverter;
import org.dspace.app.rest.converter.LoginCounterConverter;
import org.dspace.app.rest.converter.WorkflowProcessReferenceDocConverter;
import org.dspace.app.rest.enums.WorkFlowStatus;
import org.dspace.app.rest.model.*;
import org.dspace.app.rest.repository.BundleRestRepository;
import org.dspace.app.rest.utils.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.content.service.*;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.service.EPersonService;
import org.dspace.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    public SapService sapService;


    @Autowired
    public WorkflowProcessReferenceDocConverter workflowProcessReferenceDocConverter;
    @Autowired
    ConfigurationService configurationService;

    @Autowired
    WorkflowProcessNoteService workflowProcessNoteService;
    @Autowired
    private FeedbackService feedbackService;
    @Autowired
    LoginCounterService loginCounterService;

    @Autowired
    WorkFlowProcessHistoryService workFlowProcessHistoryService;

    @Autowired
    WorkFlowProcessMasterValueService workFlowProcessMasterValueService;


    @Autowired
    EpersonToEpersonMappingService epersonToEpersonMappingService;


    @Autowired
    WorkFlowProcessMasterService workFlowProcessMasterService;

    @Autowired
    WorkFlowProcessDraftDetailsService workFlowProcessDraftDetailsService;

    @Autowired
    LoginCounterConverter loginCounterConverter;

    @Autowired
    BitstreamService bitstreamService;

    @Autowired
    private BundleRestRepository bundleRestRepository;

    @Autowired
    private WorkflowProcessService workflowProcessService;

    @Autowired
    WorkflowProcessReferenceDocService workflowProcessReferenceDocService;

    @Autowired
    WorkflowProcessReferenceDocVersionService workflowProcessReferenceDocVersionService;


    @Autowired
    ItemConverter itemConverter;
    @Autowired
    DigitalSign digitalSign;

    @Autowired
    EPersonService ePersonService;


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

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD}, value = "/getFileNumber")
    public Map<String, String> getFileNumber(HttpServletRequest request) throws Exception {
        String filenumber = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            EPerson currentuser = context.getCurrentUser();
            StringBuffer sb = new StringBuffer();
            sb.append("F/PMML/");
            WorkFlowProcessMasterValue department;
            if (currentuser != null) {
                Optional<EpersonToEpersonMapping> map = currentuser.getEpersonToEpersonMappings().stream().filter(d -> d.getIsactive() == true).findFirst();
                if (map.isPresent()) {
                    sb.append(map.get().getEpersonmapping().getDepartment().getPrimaryvalue());
                }
            }
            sb.append("/" + DateUtils.getFinancialYear());
            int count = 0;
            count = workflowProcessService.getNextFileNumber(context);
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

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD}, value = "/callSAPPost")
    public SAPResponse SAPCallPOst(@PathVariable UUID uuid, HttpServletRequest request) throws Exception {
        SAPResponse sapResponse = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            String documentno = "51056001142024";
            JCoDestination destination = sapService.getDestination();
            if (destination != null && destination.isValid()) {
                JCoFunction jCoFunction = sapService.getFunctionZDMS_DOCUMENT_POST(destination);
                sapResponse = sapService.executeSAP(jCoFunction, destination, documentno);
                return sapResponse;
            }
        } catch (Exception e) {
            System.out.println("in error CallSap    ::" + e.getMessage());
            sapResponse.setMESSAGE(e.getMessage());
            sapResponse.setMSGTYP("E");
        }
        return sapResponse;
    }

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD}, value = "/getFileNumber1")
    public Map<String, String> getFileNumber1(HttpServletRequest request, @Parameter(value = "department", required = true) String department, @Parameter(value = "subject", required = true) String subject) throws Exception {
        String filenumber = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            StringBuffer sb = new StringBuffer();
            sb.append("PMML/" + department);
            sb.append("/" + DateUtils.getFinancialYear());
            int count = workflowProcessService.getNextFileNumber(context);
            count = count + 1;
            sb.append("/0000" + count + "/" + subject);
            filenumber = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in getInwardNumber ");
        }
        Map<String, String> map = new HashMap<>();
        map.put("filenumber", filenumber);
        return map;
    }

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD}, value = "/getYear")
    public Map<String, String> getYear(HttpServletRequest request) throws Exception {
        try {
            Map<String, String> map = new HashMap<>();
            map.put("year", DateUtils.getFinancialYear());
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in get Year ");
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/downloadItemReport")
    public ResponseEntity<Resource> downloadItem(HttpServletRequest request,
                                                 @Parameter(value = "startdate", required = true) String startdate,
                                                 @Parameter(value = "enddate", required = true) String enddate) {
        try {
            Context context = ContextUtil.obtainContext(request);
            String filename = "ProductivityReport.xlsx";
            List<Item> list = itemService.getDataTwoDateRangeDownload(context, startdate, enddate);
            System.out.println("size" + list.size());
            List<ExcelDTO> listDTo = list.stream().map(i -> {
                String title = itemService.getMetadataFirstValue(i, "dc", "title", null, null);
                String type = itemService.getMetadataFirstValue(i, "casefile", "case", "typename", null);
                String issued = itemService.getMetadataFirstValue(i, "casefile", "case", "registrationyear", null);
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


    @RequestMapping(method = RequestMethod.GET, value = "/downloadfilterDepartmentWiseCount")
    public ResponseEntity<Resource> downloadfilterDepartmentWiseCount(HttpServletRequest request,
                                                                      @Parameter(value = "startdate", required = true) String startdate,
                                                                      @Parameter(value = "enddate", required = true) String enddate,
                                                                      @Parameter(value = "type", required = true) String type,
                                                                      @Parameter(value = "priority", required = true) String priority,
                                                                      @Parameter(value = "status", required = true) String status) {
        try {
            Context context = ContextUtil.obtainContext(request);
            HashMap<String, String> map = new HashMap<>();
            String name = "_";
            if (priority != null && !priority.isEmpty()) {
                map.put("priority", priority);
            }
            if (status != null && !status.isEmpty()) {
                map.put("status", status);
            }
            if (type != null && !type.isEmpty()) {
                map.put("type", type);
                WorkFlowProcessMasterValue wm = workFlowProcessMasterValueService.find(context, UUID.fromString(type));
                if (wm != null) {
                    if (wm.getPrimaryvalue().equalsIgnoreCase("Draft")) {
                        map.put("draft", "draft");
                        name = "EFile";
                    } else {
                        map.put("tapal", "tapal");
                        name = "Tapal";
                    }
                }
            }
            System.out.println(map);

            String filename = "DepartmentWiseTotal_" + name + "count.xlsx";
            List<Object[]> list = workflowProcessService.filterDepartmentWiseCountDownload(context, map, startdate, enddate);
            System.out.println("List----" + list.size());
            List<DepartmentDTO> rests = list.stream().filter(d -> d[0] != null && d[1] != null).map(d -> {
                DepartmentDTO dd = new DepartmentDTO();
                dd.setName(d[0].toString());
                dd.setCount(Long.parseLong(d[1].toString()));
                return dd;
            }).collect(Collectors.toList());
            ByteArrayInputStream in = ExcelHelper.tutorialsToExceldEPARTMENT(rests);
            InputStreamResource file = new InputStreamResource(in);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);
        } catch (SQLException e) {
            System.out.println("error :" + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    @RequestMapping(method = RequestMethod.GET, value = "/userlist")
    public ResponseEntity<Resource> userlist(HttpServletRequest request) {
        try {
            Context context = ContextUtil.obtainContext(request);
            HashMap<String, String> map = new HashMap<>();


            String filename = "userlist.xlsx";
            List<EPerson> list = ePersonService.getall(context);

            ByteArrayInputStream in = ExcelHelper.tutorialsToExceldEpersion(list);
            InputStreamResource file = new InputStreamResource(in);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);
        } catch (SQLException e) {
            System.out.println("error :" + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    @RequestMapping(method = RequestMethod.GET, value = "/getDepartmentWiseNoOfProcessWorkflowCounts")
    public ResponseEntity<Resource> getDepartmentWiseNoOfProcessWorkflowCounts(HttpServletRequest request,
                                                                               @Parameter(value = "startdate", required = true) String startdate,
                                                                               @Parameter(value = "enddate", required = true) String enddate,
                                                                               @Parameter(value = "workflowtype", required = true) String workflowtype) {
        try {
            Context context = ContextUtil.obtainContext(request);
            String filename = "statisticalReport.xlsx";
            List<DepartmentDTO> DepartmentDTOLIST = new ArrayList<>();
            System.out.println("in getDepartmentWiseNoOfProcessWorkflowCounts ");
            if (workflowtype.equalsIgnoreCase("Draft") || workflowtype.equalsIgnoreCase("Inward")) {
                List<Object[]> list = itemService.getDepartmentWiseNoOfProcessWorkflowCounts(context, startdate, enddate, workflowtype);
                System.out.println("size" + list.size());
                if (list != null) {
                    for (Object[] result : list) {
                        DepartmentDTO modeldto = new DepartmentDTO();
                        String name = (String) result[0];
                        Long count = (Long) (result[1] != null ? result[1] : 0);
                        modeldto.setName(name);
                        modeldto.setCount(count);
                        DepartmentDTOLIST.add(modeldto);
                    }
                }
                ByteArrayInputStream in = ExcelHelper.tutorialsToExceldEPARTMENT(DepartmentDTOLIST);
                InputStreamResource file = new InputStreamResource(in);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                        .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                        .body(file);
            } else {
                throw new RuntimeException("Enter Valid WorkflowType");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static boolean isNullOrEmptyOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static ResponseEntity<byte[]> bytesToWebResponse(byte[] bytes, String docName) throws IOException {
        return bytesToWebResponse(bytes, docName, MediaType.APPLICATION_PDF);
    }

    public static ResponseEntity<byte[]> bytesToWebResponse(byte[] bytes, String docName, MediaType mediaType) throws IOException {

        // Return the PDF as a response
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentLength(bytes.length);
        String encodedDocName = URLEncoder.encode(docName, StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");
        headers.setContentDispositionFormData("attachment", encodedDocName);
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    public static Date DateToSTRDDMMYYYHHMMSS(Date date) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String s = formatter.format(date);
        return new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").parse(s);
    }

    private static String generateRandomText(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }
}
