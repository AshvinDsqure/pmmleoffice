/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoFunction;
import org.apache.commons.io.IOUtils;
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
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.io.pem.PemReader;
import org.dspace.app.rest.converter.ItemConverter;
import org.dspace.app.rest.converter.LoginCounterConverter;
import org.dspace.app.rest.converter.WorkflowProcessReferenceDocConverter;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
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
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD}, value = "/callSAPPost")
    public SAPResponse SAPCallPOst(@PathVariable UUID  uuid, HttpServletRequest request) throws Exception {
        SAPResponse sapResponse=null;
        try {
            Context context = ContextUtil.obtainContext(request);
            String documentno="51056001142024";
            JCoDestination destination= sapService.getDestination();
            if (destination != null && destination.isValid()) {
                JCoFunction jCoFunction=  sapService.getFunctionZDMS_DOCUMENT_POST(destination);
                sapResponse=sapService.executeSAP(jCoFunction,destination,documentno);
                return sapResponse;
            }
        }catch (Exception e){
            System.out.println("in error CallSap    ::"+e.getMessage());
            sapResponse.setMESSAGE(e.getMessage());
            sapResponse.setMSGTYP("E");
        }
        return sapResponse;
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
            System.out.println("size"+list.size());
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
    public Map<String, String> signPdf(MultipartFile certFile,
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

    @RequestMapping(method = RequestMethod.GET, value = "/digitalSignHighCourtofBihar")
    public Map<String, String> digitalSignHighCourtofBihar(HttpServletRequest request, @Parameter(value = "bitstreamid", required = true) String bitstreamid) throws SQLException, AuthorizeException, ParseException {
        Context context = ContextUtil.obtainContext(request);
        DigitalSignRequet digitalSignRequet = new DigitalSignRequet();
        InputStream pkcs12File = null;
        InputStream certFile = null;
        InputStream fileInputa = null;
        Bitstream bitstream = null;
        try {
            String certType = configurationService.getProperty("digital.sign.certtype");
            String password = configurationService.getProperty("digital.sign.password");
            String showSignature = configurationService.getProperty("digital.sign.showsignature");
            String reason = configurationService.getProperty("digital.sign.reason");
            String location = configurationService.getProperty("digital.sign.location");
            String name = configurationService.getProperty("digital.sign.name");
            String pageNumber = configurationService.getProperty("digital.sign.pagenumber");
            File p12 = new File(configurationService.getProperty("digital.sign.p12File"));
            File cert = new File(configurationService.getProperty("digital.sign.p12File"));
            pkcs12File = new FileInputStream(p12);
            certFile = new FileInputStream(cert);
            if (cert != null) {
                digitalSignRequet.setCertFileName(cert.getName());
            }
            if (p12 != null) {
                digitalSignRequet.setP12FileName(p12.getName());
            }
            bitstream = bitstreamService.find(context, UUID.fromString(bitstreamid));
            if (bitstream != null) {
                digitalSignRequet.setFileInputName(bitstream.getName());
                fileInputa = bitstreamService.retrieve(context, bitstream);
                if (fileInputa != null) {
                    digitalSignRequet.setFileInput(fileInputa);
                }
            }
            System.out.println("bitstreamid :" + bitstreamid);
            System.out.println("certFile :" + certFile);
            System.out.println("pdf :" + fileInputa);
            System.out.println("p12File :" + pkcs12File);
            System.out.println("password :" + password);
            System.out.println("certType :" + certType);
            System.out.println("location :" + location);
            System.out.println("pageNumber :" + pageNumber);
            System.out.println("reason :" + reason);
            System.out.println("name :" + name);
            System.out.println("p12.getName() :" + p12.getName());
            System.out.println("cert.getName() :" + cert.getName());

            if (!isNullOrEmptyOrBlank(certType)) {
                digitalSignRequet.setCertType(certType);
            }
            if (!isNullOrEmptyOrBlank(password)) {
                digitalSignRequet.setPassword(password);
            }
            if (!isNullOrEmptyOrBlank(showSignature)) {
                digitalSignRequet.setShowSignature(showSignature);
            }
            if (!isNullOrEmptyOrBlank(reason)) {
                digitalSignRequet.setReason(reason);
            }
            if (!isNullOrEmptyOrBlank(location)) {
                digitalSignRequet.setLocation(location);
            }
            if (!isNullOrEmptyOrBlank(name)) {
                digitalSignRequet.setName(name);
            }
            if (!isNullOrEmptyOrBlank(pageNumber)) {
                digitalSignRequet.setPageNumber(pageNumber);
            }
            if (pkcs12File != null) {
                digitalSignRequet.setP12File(pkcs12File);
            }
            if (certFile != null) {
                digitalSignRequet.setCertFile(certFile);
            }
            //System.out.println(signPDFWithCert(context,digitalSignRequet));
          // return digitalSign.digitalSignData(context,digitalSignRequet,bitstream);
       return digitalSignData(context, digitalSignRequet, bitstream);
           // return  null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static boolean isNullOrEmptyOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    @RequestMapping(method = RequestMethod.POST, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE}, value = "/addSingInNote")
    public Map<String, String> addSingInNote(MultipartFile certFile,
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
                    File tempFile1html = new File(TEMP_DIRECTORY, "sing_note" + randomNumber + ".pdf");
                    if (!tempFile1html.exists()) {
                        try {
                            tempFile1html.createNewFile();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    Item item = itemService.find(context, UUID.fromString(signPdfRequestDTO.getItemuuid()));
                    List<WorkflowProcessReferenceDoc> list = signPdfRequestDTO.getWorkflowProcessReferenceDocsRests().stream().map(d -> {
                        try {
                            return workflowProcessReferenceDocConverter.convertByService(context, d);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toList());

                    fileInputa = createNote(context, workflowProcessReferenceDoc, list, item, tempFile1html);
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

    public Map<String, String> digitalSignData(Context context, DigitalSignRequet requestModel, Bitstream bitstream) {
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

            String url = configurationService.getProperty("digital.sign.url");
            HttpPost httpPost = new HttpPost(url);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            // Add parameters as form data
            builder.addTextBody("certType", requestModel.getCertType(), ContentType.TEXT_PLAIN);
            builder.addTextBody("showSignature", requestModel.getShowSignature(), ContentType.TEXT_PLAIN);
            builder.addTextBody("location", requestModel.getLocation(), ContentType.TEXT_PLAIN);
            builder.addTextBody("reason", requestModel.getReason(), ContentType.TEXT_PLAIN);
            builder.addTextBody("pageNumber", requestModel.getPageNumber(), ContentType.TEXT_PLAIN);
            builder.addTextBody("name", requestModel.getName(), ContentType.TEXT_PLAIN);
            builder.addTextBody("password", requestModel.getPassword(), ContentType.TEXT_PLAIN);
            // Add a binary file
            builder.addBinaryBody("fileInput", requestModel.getFileInput(), ContentType.APPLICATION_OCTET_STREAM, requestModel.getFileInputName());
            builder.addBinaryBody("p12File", requestModel.getP12File(), ContentType.APPLICATION_OCTET_STREAM, requestModel.getP12FileName());
            builder.addBinaryBody("certFile", requestModel.getCertFile(), ContentType.APPLICATION_OCTET_STREAM, requestModel.getCertFileName());
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
                    Bitstream bitstreampdfsing = bundleRestRepository.processBitstreamCreationWithoutBundle1(context, pdfFileInputStream, "", tempsingpdf.getName(), bitstream);
                    if (bitstreampdfsing != null) {
                        Map<String, String> map = new HashMap<>();
                        map.put("bitstreampid", bitstreampdfsing.getID().toString());
                        System.out.println("Sing Doc Paths::" + tempsingpdf.getAbsolutePath());
                        context.commit();
                        return map;
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

    public Map<String, String> singData(Context context, SignPdfRequestDTO signPdfRequestDTO, InputStream fileInput,
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
            String url = configurationService.getProperty("digital.sign.url");
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
                            Map<String, String> map = new HashMap<>();
                            map.put("bitstreampid", bitstreampdfsing.getID().toString());
                            List<WorkflowProcessReferenceDocVersion> workflowProcessReferenceDocVersions = workflowProcessReferenceDocVersionService.getDocVersionBydocumentID(context, workflowProcessReferenceDoc1.getID(), 0, 20);
                            Optional<WorkflowProcessReferenceDocVersion> workflowProcessReferenceDocVersion = workflowProcessReferenceDocVersions.stream().filter(d -> d.getIsactive()).findFirst();
                            if (workflowProcessReferenceDocVersion.isPresent()) {
                                WorkflowProcessReferenceDocVersion workflowProcessReferenceDocVersion1 = workflowProcessReferenceDocVersionService.find(context, workflowProcessReferenceDocVersion.get().getID());
                                if (workflowProcessReferenceDocVersion1 != null && bitstreampdfsing != null) {
                                    workflowProcessReferenceDocVersion1.setBitstream(bitstreampdfsing);
                                }
                                workflowProcessReferenceDocVersionService.update(context, workflowProcessReferenceDocVersion1);
                            }
                            System.out.println("Sing Doc Paths::" + tempsingpdf.getAbsolutePath());
                            context.commit();
                            return map;
                        }
                    }
                    // Process the response content here
                } else {
                    System.out.println("error with " + statusCode);
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

    public ResponseEntity<byte[]> signPDFWithCert(Context context,DigitalSignRequet request) throws Exception {
        InputStream pdf = request.getFileInput();
        String certType = request.getCertType();
        InputStream privateKeyFile = request.getP12File();
        InputStream certFile = request.getCertFile();
        InputStream p12File = request.getP12File();
        String password = request.getPassword();
        Boolean showSignature = (request.getShowSignature().equalsIgnoreCase("on")?true:false);
        String reason = request.getReason();
        String location = request.getLocation();
        String name = request.getName();
        Integer pageNumber = Integer.parseInt(request.getPageNumber());
        PrivateKey privateKey = null;
        X509Certificate cert = null;

        System.out.println("certFile"+request.getCertFileName());
        System.out.println("pdf"+request.getFileInputName());
        System.out.println("p12File"+request.getP12FileName());
        System.out.println("password"+password);
        System.out.println("certType"+certType);
        System.out.println("location"+location);
        System.out.println("pageNumber"+pageNumber);
        System.out.println("reason"+reason);
        System.out.println("name"+name);

        if (certType != null) {
            //logger.info("Cert type provided: {}", certType);
            switch (certType) {
                case "PKCS12":
                    if (p12File != null) {
                        System.out.println("2");
                        KeyStore ks = KeyStore.getInstance("PKCS12");
                        ks.load(new ByteArrayInputStream(p12File.readAllBytes()), password.toCharArray());
                        String alias = ks.aliases().nextElement();
                        if (!ks.isKeyEntry(alias)) {
                            throw new IllegalArgumentException("The provided PKCS12 file does not contain a private key.");
                        }
                        privateKey = (PrivateKey) ks.getKey(alias, password.toCharArray());
                        cert = (X509Certificate) ks.getCertificate(alias);
                    }
                    break;
                case "PEM":
                    if (privateKeyFile != null && certFile != null) {
                        System.out.println("3");
                        // Load private key
                        KeyFactory keyFactory = KeyFactory.getInstance("RSA", BouncyCastleProvider.PROVIDER_NAME);
                        if (isPEM(privateKeyFile.readAllBytes())) {
                            privateKey = keyFactory
                                    .generatePrivate(new PKCS8EncodedKeySpec(parsePEM(privateKeyFile.readAllBytes())));
                        } else {
                            privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyFile.readAllBytes()));
                        }

                        // Load certificate
                        CertificateFactory certFactory = CertificateFactory.getInstance("X.509",
                                BouncyCastleProvider.PROVIDER_NAME);
                        if (isPEM(certFile.readAllBytes())) {
                            cert = (X509Certificate) certFactory
                                    .generateCertificate(new ByteArrayInputStream(parsePEM(certFile.readAllBytes())));
                        } else {
                            cert = (X509Certificate) certFactory
                                    .generateCertificate(new ByteArrayInputStream(certFile.readAllBytes()));
                        }
                    }
                    break;
            }
        }
        PDSignature signature = new PDSignature();
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE); // default filter
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_SHA1);
        signature.setName(name);
        signature.setLocation(location);
        signature.setReason(reason);
        signature.setSignDate(Calendar.getInstance());

        // Load the PDF
        try (PDDocument document = PDDocument.load(pdf.readAllBytes())) {
         //   logger.info("Successfully loaded the provided PDF");
            SignatureOptions signatureOptions = new SignatureOptions();

            // If you want to show the signature

            // ATTEMPT 2
            if (showSignature != null && showSignature) {
                PDPage page = document.getPage(pageNumber - 1);

                PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
                if (acroForm == null) {
                    acroForm = new PDAcroForm(document);
                    document.getDocumentCatalog().setAcroForm(acroForm);
                }

                // Create a new signature field and widget

                PDSignatureField signatureField = new PDSignatureField(acroForm);
                PDAnnotationWidget widget = signatureField.getWidgets().get(0);
                PDRectangle rect = new PDRectangle(100, 100, 300, 100); // Define the rectangle size here
                widget.setRectangle(rect);
                page.getAnnotations().add(widget);

                // Set the appearance for the signature field
                PDAppearanceDictionary appearanceDict = new PDAppearanceDictionary();
                PDAppearanceStream appearanceStream = new PDAppearanceStream(document);
                appearanceStream.setResources(new PDResources());
                appearanceStream.setBBox(rect);
                appearanceDict.setNormalAppearance(appearanceStream);
                widget.setAppearance(appearanceDict);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, appearanceStream)) {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.newLineAtOffset(110, 130);
                    contentStream.showText("Digitally signed by: " + (name != null ? name : "Unknown"));
                    contentStream.newLineAtOffset(0, -15);
                    contentStream.showText("Date: " + new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z").format(new Date()));
                    contentStream.newLineAtOffset(0, -15);
                    if (reason != null && !reason.isEmpty()) {
                        contentStream.showText("Reason: " + reason);
                        contentStream.newLineAtOffset(0, -13);
                    }
                    if (location != null && !location.isEmpty()) {
                        contentStream.showText("Location: " + location);
                        contentStream.newLineAtOffset(0, -15);
                    }
                    contentStream.endText();
                }

                // Add the widget annotation to the page
                page.getAnnotations().add(widget);

                // Add the signature field to the acroform
                acroForm.getFields().add(signatureField);

                // Handle multiple signatures by ensuring a unique field name
                String baseFieldName = "Signature";
                String signatureFieldName = baseFieldName;
                int suffix = 1;
                while (acroForm.getField(signatureFieldName) != null) {
                    suffix++;
                    signatureFieldName = baseFieldName + suffix;
                }
                signatureField.setPartialName(signatureFieldName);
            }

            document.addSignature(signature, signatureOptions);
           // logger.info("Signature added to the PDF document");
            // External signing
            ExternalSigningSupport externalSigning = document
                    .saveIncrementalForExternalSigning(new ByteArrayOutputStream());

            byte[] content = IOUtils.toByteArray(externalSigning.getContent());

            // Using BouncyCastle to sign
            CMSTypedData cmsData = new CMSProcessableByteArray(content);

            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            BouncyCastleProvider d=new BouncyCastleProvider();
            d.getProperty(BouncyCastleProvider.PROVIDER_NAME);
            ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME).build(privateKey);

            gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
                    new JcaDigestCalculatorProviderBuilder().setProvider(BouncyCastleProvider.PROVIDER_NAME).build())
                    .build(signer, cert));

            gen.addCertificates(new JcaCertStore(Collections.singletonList(cert)));
            CMSSignedData signedData = gen.generate(cmsData, false);

            byte[] cmsSignature = signedData.getEncoded();
           // logger.info("About to sign content using BouncyCastle");
            externalSigning.setSignature(cmsSignature);
           // logger.info("Signature set successfully");
            // After setting the signature, return the resultant PDF
            try (ByteArrayOutputStream signedPdfOutput = new ByteArrayOutputStream()) {
                document.save(signedPdfOutput);
                //document.save("D://"+pdf.getOriginalFilename()+".pdf");
                return boasToWebResponse(signedPdfOutput,
                        request.getFileInputName().replaceFirst("[.][^.]+$", "") + "_signed.pdf");

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ResponseEntity<byte[]> boasToWebResponse(ByteArrayOutputStream baos, String docName) throws IOException {
        return bytesToWebResponse(baos.toByteArray(), docName);
    }
    public static ResponseEntity<byte[]> bytesToWebResponse(byte[] bytes, String docName) throws IOException {
        return bytesToWebResponse(bytes, docName,MediaType.APPLICATION_PDF);
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

    private byte[] parsePEM(byte[] content) throws IOException {
        PemReader pemReader = new PemReader(new InputStreamReader(new ByteArrayInputStream(content)));
        return pemReader.readPemObject().getContent();
    }

    private boolean isPEM(byte[] content) {
        String contentStr = new String(content);
        return contentStr.contains("-----BEGIN") && contentStr.contains("-----END");
    }

    public InputStream createNote(Context context, WorkflowProcessReferenceDoc notsheetdoc, List<WorkflowProcessReferenceDoc> workflowProcessReferenceDocs, Item item, File tempFile1html) throws
            Exception {
        boolean isTextEditorFlow = false;
        WorkflowProcessReferenceDocVersion version = null;
        Map<String, Object> map = new HashMap<String, Object>();

        final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
        System.out.println("start.......createNote");
        StringBuffer sb = new StringBuffer("<!DOCTYPE html>\n" + "<html>\n" + "<head><style>@page{size:A4;margin: 0;}</style>\n" + "<title>Note</title>\n" + "</head>\n" + "<body style=\"padding-right: 20px;padding-left: 20px;background-color:#d0cece;\">");
        long notecount = 0;
        if (item != null) {
            UUID statusid = WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context).get().getID();
            notecount = workflowProcessNoteService.getNoteCountNumber(context, item.getID(), statusid);
            map.put("notecount", notecount);
        }
        notecount = notecount + 1;
        System.out.println("start.......createFinalNote" + tempFile1html.getAbsolutePath());
        //Items
        sb.append("<p><u> <b>Note # " + notecount + "</b></u></p>");

        if (notsheetdoc != null) {
            isTextEditorFlow = true;
            sb.append("<div>" + notsheetdoc.getEditortext() + "</div>");
        }
        //manager
        if (context.getCurrentUser() != null) {
            EPerson creator = context.getCurrentUser();
            String Designation1 = workFlowProcessMasterValueService.find(context, creator.getDesignation().getID()).getPrimaryvalue();
            List<String> aa = new ArrayList<>();
            aa.add(creator.getFullName());
            if (Designation1 != null) {
                aa.add(Designation1);
                sb.append("<br><br><br><div style=\"width:100%;    text-align: left;\">\n" + "<span>" + context.getCurrentUser().getFullName() + "<br>" + Designation1);
                //aa.add(DateFormate(workflowProcessReferenceDoc..getInitDate()));
                sb.append("<br>" + DateFormate(notsheetdoc.getCreatedate()) + "</span></div>");
            }
            map.put("creator", aa);
        }
        Map<String, String> referencedocumentmap = null;
        sb.append("<br><br><div style=\"width:100%;\"> ");
        sb.append("<div style=\"width:70%;  float:left;\"> <p><b>Attachment :</b></p> ");
        //Reference Documents dinamix
        List<Map<String, String>> listreferenceReference = new ArrayList<>();
        if (workflowProcessReferenceDocs != null && workflowProcessReferenceDocs.size() != 0) {
            for (WorkflowProcessReferenceDoc workflowProcessReferenceDoc : workflowProcessReferenceDocs) {
                referencedocumentmap = new HashMap<String, String>();
                if (workflowProcessReferenceDoc.getDrafttype() != null && workflowProcessReferenceDoc.getDrafttype().getPrimaryvalue() != null && workflowProcessReferenceDoc.getDrafttype().getPrimaryvalue().equals("Reference Document")) {
                    // InputStream out = null;
                    if (workflowProcessReferenceDoc.getBitstream() != null) {
                        String baseurl = configurationService.getProperty("dspace.server.url");
                        referencedocumentmap.put("name", FileUtils.getNameWithoutExtension(workflowProcessReferenceDoc.getBitstream().getName()));
                        referencedocumentmap.put("link", baseurl + "/api/core/bitstreams/" + workflowProcessReferenceDoc.getBitstream().getID() + "/content");
                        sb.append("<span style=\"text-align: left;\"><a href=" + baseurl + "/api/core/bitstreams/" + workflowProcessReferenceDoc.getBitstream().getID() + "/content>");
                        if (!isTextEditorFlow) {
                            stroremetadateinmap(workflowProcessReferenceDoc.getBitstream(), referencedocumentmap);
                            listreferenceReference.add(referencedocumentmap);
                        } else {
                            stroremetadate(workflowProcessReferenceDoc.getBitstream(), sb);
                        }
                    }
                }

            }
        }

        sb.append("</div>");
        map.put("Reference Documents", listreferenceReference);
        sb.append("<div style=\"width:23%;float:right;\"> <p><b>Signature</b></p> ");
        Map<String, String> referencenottingmap = null;
        List<Map<String, String>> listreferencenotting = new ArrayList<>();
        if (workflowProcessReferenceDocs != null && workflowProcessReferenceDocs.size() != 0) {
            for (WorkflowProcessReferenceDoc workflowProcessReferenceDoc : workflowProcessReferenceDocs) {
                referencenottingmap = new HashMap<String, String>();
                if (workflowProcessReferenceDoc.getDrafttype() != null && workflowProcessReferenceDoc.getDrafttype().getPrimaryvalue() != null && workflowProcessReferenceDoc.getDrafttype().getPrimaryvalue().equals("Reference Noting")) {
                    if (workflowProcessReferenceDoc.getBitstream() != null) {
                        String baseurl = configurationService.getProperty("dspace.server.url");
                        referencenottingmap.put("name1", FileUtils.getNameWithoutExtension(workflowProcessReferenceDoc.getBitstream().getName()));
                        referencenottingmap.put("link", baseurl + "/api/core/bitstreams/" + workflowProcessReferenceDoc.getBitstream().getID() + "/content");
                        sb.append("<span style=\"text-align: left;\"><a href=" + baseurl + "/api/core/bitstreams/" + workflowProcessReferenceDoc.getBitstream().getID() + "/content>");
                        if (workflowProcessReferenceDoc.getWorkflowprocessnote() != null && workflowProcessReferenceDoc.getWorkflowprocessnote().getID() != null) {
                            WorkflowProcessNote note = workflowProcessNoteService.find(context, workflowProcessReferenceDoc.getWorkflowprocessnote().getID());
                            if (note != null) {
                                StringBuffer notecreateor = new StringBuffer("Note Creator: ");
                                if (workflowProcessReferenceDoc.getBitstream().getName() != null) {
                                    sb.append(FileUtils.getNameWithoutExtension(workflowProcessReferenceDoc.getBitstream().getName()) + "</a>");
                                } else {
                                    sb.append("-</a>");
                                }
                                if (note.getSubject() != null) {
                                    referencenottingmap.put("subject", note.getSubject());
                                    sb.append("<br>" + note.getSubject() + "<br>");
                                } else {
                                    sb.append("<br>-<br>");
                                }
                                if (note.getSubmitter() != null && note.getSubmitter().getFullName() != null) {
                                    sb.append("Note Creator: " + note.getSubmitter().getFullName());
                                    notecreateor.append(note.getSubmitter().getFullName() + " ");
                                } else {
                                    sb.append("Note Creator:<br>-");
                                }
                                if (note.getSubmitter() != null && note.getSubmitter().getDesignation() != null && note.getSubmitter().getDesignation().getID() != null) {
                                    String Designation1 = workFlowProcessMasterValueService.find(context, note.getSubmitter().getDesignation().getID()).getPrimaryvalue();
                                    if (Designation1 != null) {
                                        sb.append(" | " + Designation1);
                                        notecreateor.append("|" + Designation1);
                                    } else {
                                        sb.append(" | -");
                                    }
                                }
                                if (note.getInitDate() != null) {
                                    sb.append(" " + DateFormate(note.getInitDate()));
                                    notecreateor.append(" " + DateFormate(note.getInitDate()));
                                } else {
                                    sb.append(" - ");
                                }
                                referencenottingmap.put("notecreateor", notecreateor.toString());

                            }
                            if (workflowProcessReferenceDoc.getItemname() != null) {
                                sb.append("<br>" + workflowProcessReferenceDoc.getItemname());
                                referencenottingmap.put("filename", workflowProcessReferenceDoc.getItemname());
                            }
                            sb.append("</span><br><br>");
                        }
                    }
                    listreferencenotting.add(referencenottingmap);
                }
            }
        }
        sb.append("</div></div><br>");
        map.put("Reference Noting", listreferencenotting);
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

    public void stroremetadate(Bitstream bitstream, StringBuffer sb) throws ParseException {
        if (bitstream.getMetadata() != null) {
            int i = 0;
            String refnumber = null;
            String doctype = null;
            String date = null;
            String lettercategory = null;
            String lettercategoryhindi = null;
            String description = null;
            for (MetadataValue metadataValue : bitstream.getMetadata()) {
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_doc_type")) {
                    doctype = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_ref_number")) {
                    refnumber = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_date")) {
                    date = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_letter_category")) {
                    lettercategory = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_letter_categoryhi")) {
                    lettercategoryhindi = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_description")) {
                    description = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_title")) {
                }
                i++;
            }

            if (doctype != null) {
                sb.append(doctype + "</a>");
            } else {
                if (bitstream.getName() != null) {
                    sb.append(FileUtils.getNameWithoutExtension(bitstream.getName()) + "</a>");

                } else {
                    sb.append("-</a>");
                }
            }
            if (refnumber != null) {
                sb.append(" (" + refnumber + ")");
            } else {
                sb.append("-");
            }
            if (date != null) {

                try {
                    sb.append("<br>" + DateUtils.strDateToString(date));
                } catch (Exception e) {
                }
            } else {
                sb.append("<br>-");
            }
            if (lettercategory != null && lettercategoryhindi != null) {
                sb.append(" (" + lettercategory + "|" + lettercategoryhindi + ")");
            } else {
                sb.append("(-)");
            }
            if (description != null) {
                sb.append("<br>" + description);
            } else {
                sb.append("<br> -");
            }
            sb.append("</span><br><br>");
        }

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
        String logopath = configurationService.getProperty("pcmc.acknowledgement.logo");
        if(logopath!=null) {
            sb.append("<center><img src=" + logopath + " style=\"margin:20px; height:200px;\"></center>");
        }

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

    public void stroremetadateinmap(Bitstream bitstream, Map<String, String> map) throws ParseException {
        if (bitstream.getMetadata() != null) {
            int i = 0;
            String refnumber = null;
            String doctype = null;
            String date = null;
            String lettercategory = null;
            String lettercategoryhindi = null;
            String description = null;
            StringBuffer doctyperefnumber = new StringBuffer();
            StringBuffer datelettercategory = new StringBuffer();

            for (MetadataValue metadataValue : bitstream.getMetadata()) {
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_doc_type")) {
                    doctype = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_ref_number")) {
                    refnumber = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_date")) {
                    date = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_letter_category")) {
                    lettercategory = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_letter_categoryhi")) {
                    lettercategoryhindi = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_description")) {
                    description = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_title")) {
                }
                i++;
            }

            if (doctype != null) {

                doctyperefnumber.append(doctype);
            } else {
                if (bitstream.getName() != null) {

                } else {

                }
            }
            if (refnumber != null) {

                doctyperefnumber.append("(" + refnumber + ")");
            } else {

            }
            if (date != null) {

                datelettercategory.append(DateUtils.strDateToString(date));
            } else {

            }
            if (lettercategory != null && lettercategoryhindi != null) {

                datelettercategory.append(" (" + lettercategory + "|" + lettercategoryhindi + ")");
            } else {

            }
            if (description != null) {

                map.put("description", description);
            } else {

            }
            map.put("datelettercategory", datelettercategory.toString() != null ? datelettercategory.toString() : "-");
            map.put("doctyperefnumber", doctyperefnumber.toString() != null ? doctyperefnumber.toString() : "-");

        }

    }

    public Boolean updateDraftandsetIssinglatter(Context context, WorkFlowProcessDraftDetails draftDetails) {
        try {
            draftDetails.setIssinglatter(true);
            workFlowProcessDraftDetailsService.update(context, draftDetails);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String DateFormate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        return formatter.format(date);
    }

    public void storehistory(Context context, WorkflowProcessReferenceDoc workflowProcessReferenceDoc) {
        try {
            //add Notsheet  Histoy
            WorkflowProcess workflowProcess = workflowProcessReferenceDoc.getWorkflowProcess();
            WorkFlowProcessDraftDetails draftDetails = workflowProcess.getWorkFlowProcessDraftDetails();
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
