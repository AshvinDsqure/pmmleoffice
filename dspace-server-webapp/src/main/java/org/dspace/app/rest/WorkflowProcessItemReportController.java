/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.io.pem.PemReader;
import org.dspace.app.rest.converter.ItemConverter;
import org.dspace.app.rest.converter.LoginCounterConverter;
import org.dspace.app.rest.enums.WorkFlowAction;
import org.dspace.app.rest.enums.WorkFlowStatus;
import org.dspace.app.rest.model.*;
import org.dspace.app.rest.repository.BundleRestRepository;
import org.dspace.app.rest.utils.*;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    WorkFlowProcessHistoryService workFlowProcessHistoryService;

    @Autowired
    WorkFlowProcessMasterValueService workFlowProcessMasterValueService;

    @Autowired
    WorkFlowProcessMasterService workFlowProcessMasterService;

    @Autowired
    WorkFlowProcessDraftDetailsService  workFlowProcessDraftDetailsService;

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
    ItemConverter itemConverter;


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
            sb.append("PCMC/");
            WorkFlowProcessMasterValue department;
            if (currentuser != null) {
                department = workFlowProcessMasterValueService.find(context, context.getCurrentUser().getDepartment().getID());
                if (department.getPrimaryvalue() != null) {
                    sb.append(department.getSecondaryvalue());
                }
            }
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

    @PreAuthorize("hasPermission(#uuid, 'ITEAM', 'READ')")
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD}, value = "/getFileNumber1")
    public Map<String, String> getFileNumber1(HttpServletRequest request, @Parameter(value = "department", required = true) String department, @Parameter(value = "subject", required = true) String subject) throws Exception {
        String filenumber = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            StringBuffer sb = new StringBuffer();
            sb.append("PCMC/" + department);
            sb.append("/" + DateUtils.getFinancialYear());
            int count = itemService.countTotal(context);
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

    @PreAuthorize("hasPermission(#uuid, 'ITEAM', 'READ')")
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
    public String margedDocumentanddownload(HttpServletRequest request,
                                            @Parameter(value = "itemid", required = true) String itemid) {
        try {
            System.out.println("generateRandomText(6);:::::::::" + generateRandomText(6));
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
        return generateRandomText(6);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE}, value = "/signPdf")
    public Map<String,String> signPdf(MultipartFile certFile,
                             String singPdfRequest, HttpServletRequest request) throws SQLException, AuthorizeException, ParseException {
        Context context = ContextUtil.obtainContext(request);
        Bitstream bitstreampdf = null;
        String namepdf = "";
        Bitstream bitstreampkcs12 = null;
        InputStream fileInputa = null;
        InputStream pkcs12File = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            SignPdfRequestDTO signPdfRequestDTO = mapper.readValue(singPdfRequest, SignPdfRequestDTO.class);
            System.out.println("doc id" + signPdfRequestDTO.getDocumentuuid());
            System.out.println("getPksc12orpemdocuuid  id" + signPdfRequestDTO.getPksc12orpemdocuuid());
            WorkflowProcessReferenceDoc workflowProcessReferenceDoc = workflowProcessReferenceDocService.find(context, UUID.fromString(signPdfRequestDTO.getDocumentuuid()));
            if (workflowProcessReferenceDoc != null) {
                if (workflowProcessReferenceDoc.getEditortext() != null) {
                    final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
                    Random random = new Random();
                    // Generate a random 4-digit number
                    int randomNumber = random.nextInt(9000) + 1000;
                    File tempFile1html = new File(TEMP_DIRECTORY, "sing_" + randomNumber + ".pdf");
                    if (!tempFile1html.exists()) {
                        try {
                            tempFile1html.createNewFile();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    fileInputa = createFinalDraftDoc(context, workflowProcessReferenceDoc, tempFile1html);
                    Bitstream bitstream = bundleRestRepository.processBitstreamCreationWithoutBundle(context, fileInputa, "", tempFile1html.getName());
                    workflowProcessReferenceDoc.setBitstream(bitstream);
                    namepdf = bitstream.getName();
                    WorkflowProcessReferenceDoc d = workflowProcessReferenceDocService.create(context, workflowProcessReferenceDoc);
                    workflowProcessReferenceDoc = d;
                    fileInputa = bitstreamService.retrieve(context, workflowProcessReferenceDoc.getBitstream());
                } else {
                    System.out.println("in doc id" + workflowProcessReferenceDoc.getID());
                    bitstreampdf = workflowProcessReferenceDoc.getBitstream();
                    fileInputa = bitstreamService.retrieve(context, workflowProcessReferenceDoc.getBitstream());
                    namepdf = bitstreampdf.getName();
                }
            }
           WorkflowProcessReferenceDoc pkcs12doc = workflowProcessReferenceDocService.find(context, UUID.fromString(signPdfRequestDTO.getPksc12orpemdocuuid()));
            if (pkcs12doc != null) {
                bitstreampkcs12 = pkcs12doc.getBitstream();
                pkcs12File = bitstreamService.retrieve(context, pkcs12doc.getBitstream());
            }
            return singData(context, signPdfRequestDTO, fileInputa, pkcs12File, certFile, namepdf, bitstreampkcs12.getName(), workflowProcessReferenceDoc);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public  Map<String ,String> singData(Context context, SignPdfRequestDTO signPdfRequestDTO, InputStream fileInput,
                              InputStream p12File,
                              MultipartFile certFile, String fileInputname, String p12Filename, WorkflowProcessReferenceDoc workflowProcessReferenceDoc) {
        //CloseableHttpClient httpClient = HttpClients.createDefault();
        final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
        File tempsingpdf = new File(TEMP_DIRECTORY, "sign" + ".pdf");
        if (!tempsingpdf.exists()) {
            try {
                tempsingpdf.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        HttpClient httpClient = HttpClients.createDefault();
        try {
            //String url = "http://localhost:8081/api/v1/security/cert-sign";
            String url = "http://202.21.38.245:8084/api/v1/security/cert-sign";
            HttpPost httpPost = new HttpPost(url);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            // Add parameters as form data
            builder.addTextBody("certType", signPdfRequestDTO.getCertType(), ContentType.TEXT_PLAIN);
            builder.addTextBody("showSignature", signPdfRequestDTO.getShowSignature(), ContentType.TEXT_PLAIN);
            builder.addTextBody("location", signPdfRequestDTO.getLocation(), ContentType.TEXT_PLAIN);
            builder.addTextBody("reason", signPdfRequestDTO.getReason(), ContentType.TEXT_PLAIN);
            builder.addTextBody("pageNumber", signPdfRequestDTO.getPageNumber(), ContentType.TEXT_PLAIN);
            builder.addTextBody("name", signPdfRequestDTO.getName(), ContentType.TEXT_PLAIN);
            builder.addTextBody("password", signPdfRequestDTO.getKeystorePassword(), ContentType.TEXT_PLAIN);
            // Add a binary file
            builder.addBinaryBody("fileInput", fileInput, ContentType.APPLICATION_OCTET_STREAM, fileInputname);
            builder.addBinaryBody("p12File", certFile.getInputStream(), ContentType.APPLICATION_OCTET_STREAM, certFile.getName());
            builder.addBinaryBody("certFile", p12File, ContentType.APPLICATION_OCTET_STREAM, p12Filename);
            // Build the multipart entity
            httpPost.setEntity(builder.build());
            // Execute the request
            try {
                // Execute the request and get the response
                HttpResponse response = httpClient.execute(httpPost);
                System.out.println("Response :::::::::::::" + response);
                // Check the response status code and content
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    HttpStatus httpStatus = HttpStatus.valueOf(statusCode);
                    HttpHeaders headers = new HttpHeaders();
                    for (org.apache.http.Header header : response.getAllHeaders()) {
                        headers.add(header.getName(), header.getValue());
                    }
                    byte[] responseBody = EntityUtils.toByteArray(entity);
                    byte[] s = responseBody;
                    try (FileOutputStream fos = new FileOutputStream(new File(tempsingpdf.getAbsolutePath()))) {
                        fos.write(responseBody);
                        fos.close();
                        fos.flush();
                    }
                    System.out.println("file path" + tempsingpdf.getAbsolutePath());
                    FileInputStream pdfFileInputStream = new FileInputStream(new File(tempsingpdf.getAbsolutePath()));
                    Bitstream bitstreampdfsing = bundleRestRepository.processBitstreamCreationWithoutBundle1(context, pdfFileInputStream, "", tempsingpdf.getName(), workflowProcessReferenceDoc.getBitstream());
                    if (bitstreampdfsing != null) {
                        WorkflowProcessReferenceDoc workflowProcessReferenceDoc1 = workflowProcessReferenceDocService.find(context, UUID.fromString(signPdfRequestDTO.getDocumentuuid()));
                        if (workflowProcessReferenceDoc1 != null) {
                            Map<String ,String>map=new HashMap<>();
                            map.put("bitstreampid",bitstreampdfsing.getID().toString());
                            map.put("issinglatter" ,updateDraftandsetIssinglatter(context,workflowProcessReferenceDoc1.getWorkflowProcess().getWorkFlowProcessDraftDetails()).toString());
                            workflowProcessReferenceDoc1.setBitstream(bitstreampdfsing);
                            workflowProcessReferenceDoc1.setIssignature(true);

                            WorkflowProcessReferenceDoc processReferenceDoc1=workflowProcessReferenceDocService.create(context, workflowProcessReferenceDoc1);
                            storehistory(context,processReferenceDoc1);
                            System.out.println("Sing Doc Paths::" + tempsingpdf.getAbsolutePath());
                            context.commit();
                            return map;
                        }
                    }
                    // Process the response content here
                } else {
                    System.out.println("errot with " + statusCode);
                    HttpEntity entity = response.getEntity();
                    byte[] responseBody = EntityUtils.toByteArray(entity);
                    return null;

                }
            } catch (IOException e) {
                System.out.println("error" + e.getMessage());
                e.printStackTrace();

            }
        } catch (Exception e) {
            System.out.println("error" + e.getMessage());
            e.printStackTrace();
        }
        return null;
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

    private byte[] parsePEM(byte[] content) throws IOException {
        PemReader pemReader = new PemReader(new InputStreamReader(new ByteArrayInputStream(content)));
        return pemReader.readPemObject().getContent();
    }

    private boolean isPEM(byte[] content) {
        String contentStr = new String(content);
        return contentStr.contains("-----BEGIN") && contentStr.contains("-----END");
    }

    public FileInputStream createFinalDraftDoc(Context context, WorkflowProcessReferenceDoc workflowProcessReferenceDoc, File tempFile1html) throws Exception {
        System.out.println("in create");
        boolean isTextEditorFlow = false;
        System.out.println("start.......sing");
        StringBuffer sb = new StringBuffer("<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head><style>@page{size:A4;margin: 0;}</style>\n" +
                "<title>Note</title>\n" +
                "</head>\n" +
                "<body style=\"padding-right: 20px;padding-left: 20px;\">");
        long notecount = 1;
   /*     if (workflowProcess.getItem() != null && workflowProcess.getItem().getName() != null) {
            UUID statusid = WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context).get().getID();
            notecount = workflowProcessNoteService.getNoteCountNumber(context, workflowProcess.getItem().getID(), statusid);
            map.put("notecount", notecount);
        }*/
        notecount = notecount + 1;

        System.out.println("start.......createFinalNote" + tempFile1html.getAbsolutePath());
        //Items
        //sb.append("<p><center> <b>Latter" + notecount + "</b></center></p>");

        if (workflowProcessReferenceDoc.getEditortext() != null) {
            isTextEditorFlow = true;
            sb.append("<div>" + workflowProcessReferenceDoc.getEditortext() + "</div>");
        }
        sb.append("</body></html>");
        if (isTextEditorFlow) {
            System.out.println("::::::::::IN isTextEditorFlow :::::::::");
            FileOutputStream files = new FileOutputStream(new File(tempFile1html.getAbsolutePath()));
            System.out.println("HTML:::" + sb.toString());
            int result = PdfUtils.HtmlconvertToPdf(sb.toString(), files);
            System.out.println("HTML CONVERT DONE::::::::::::::: :" + tempFile1html.getAbsolutePath());
            FileInputStream outputfile = new FileInputStream(new File(tempFile1html.getAbsolutePath()));
            return outputfile;
        }
        return null;
    }

    public Boolean updateDraftandsetIssinglatter(Context context,WorkFlowProcessDraftDetails draftDetails){
        try {
            draftDetails.setIssinglatter(true);
            workFlowProcessDraftDetailsService.update(context,draftDetails);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public void storehistory(Context context, WorkflowProcessReferenceDoc workflowProcessReferenceDoc) {
        try {
            //add Notsheet  Histoy
            WorkflowProcess workflowProcess = workflowProcessReferenceDoc.getWorkflowProcess();
            WorkFlowProcessDraftDetails draftDetails=workflowProcess.getWorkFlowProcessDraftDetails();
            WorkFlowProcessHistory workFlowAction = new WorkFlowProcessHistory();
            WorkflowProcessEperson workflowProcessEperson = workflowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getePerson() != null).filter(d -> d.getePerson().getID().toString().equalsIgnoreCase(context.getCurrentUser().getID().toString())).findFirst().get();
            workFlowAction.setWorkflowProcessEpeople(workflowProcessEperson);
            workFlowAction.setSentto(workflowProcessEperson);
            workFlowAction.setActionDate(new Date());
            workFlowAction.setAction(getMastervalueData(context, WorkFlowAction.MASTER.getAction(), WorkFlowAction.CREATE.getAction()));
            workFlowAction.setWorkflowProcess(workflowProcess);
            workFlowAction.setComment("Document Sing By " + context.getCurrentUser().getName());
            workFlowProcessHistoryService.create(context, workFlowAction);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public WorkFlowProcessMasterValue getMastervalueData(Context context, String mastername, String mastervaluename) throws SQLException {
        WorkFlowProcessMaster workFlowProcessMaster = workFlowProcessMasterService.findByName(context, mastername);
        if (workFlowProcessMaster != null) {
            WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, mastervaluename, workFlowProcessMaster);
            if (workFlowProcessMasterValue != null) {
                System.out.println(" MAster value" + workFlowProcessMasterValue.getPrimaryvalue());
                return workFlowProcessMasterValue;
            }
        }
        return null;
    }
}
