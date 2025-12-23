/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import emh.Enum.Coordinates;
import emh.Enum.Token_Status;
import emh.Enum.Token_Type;
import emh.Model.RequestData.*;
import emh.Model.ResponseData.BulkSignOutput;
import emh.Model.ResponseData.ResponseDataListPKCSCertificate;
import emh.Model.ResponseData.ResponseDataListProviderToken;
import emh.Model.ResponseData.ResponseDataPKCSBulkSign;
import emh.emBridgeLib.emBridge;
import emh.emBridgeLib.emBridgeSignerInput;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.converter.ItemConverter;
import org.dspace.app.rest.converter.LoginCounterConverter;
import org.dspace.app.rest.converter.WorkflowProcessReferenceDocConverter;
import org.dspace.app.rest.enums.WorkFlowAction;
import org.dspace.app.rest.exception.FieldBlankOrNullException;
import org.dspace.app.rest.jbpm.JbpmServerImpl;
import org.dspace.app.rest.model.*;
import org.dspace.app.rest.repository.BundleRestRepository;
import org.dspace.app.rest.utils.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.content.service.*;
import org.dspace.core.Context;
import org.dspace.services.ConfigurationService;
import org.dspace.storage.bitstore.DSBitStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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
        + "/digital")
public class WorkflowProcessDigitalSignController {
    private static final Logger log = LogManager.getLogger();
    @Autowired
    protected Utils utils;
    @Autowired
    public ItemService itemService;

    @Autowired
    public RestTemplate restTemplate;

    @Autowired
    PdfOprationServerImpl pdfOprationServer;


    @Autowired
    public WorkflowProcessInwardController workflowProcessInwardController;


    @Autowired
    public WorkflowProcessReferenceDocConverter workflowProcessReferenceDocConverter;
    @Autowired
    ConfigurationService configurationService;


    @Autowired
    JbpmServerImpl jbpmServer;

    @Autowired
    DSBitStoreService dsBitStoreService;

    @Autowired
    private BundleRestRepository bundleRestRepository;


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
    CategoryService categoryService;

    @Autowired
    WorkFlowProcessMasterService workFlowProcessMasterService;

    @Autowired
    WorkFlowProcessDraftDetailsService workFlowProcessDraftDetailsService;

    @Autowired
    LoginCounterConverter loginCounterConverter;

    @Autowired
    BitstreamService bitstreamService;


    @Autowired
    private WorkflowProcessService workflowProcessService;

    @Autowired
    WorkflowProcessEpersonService workflowProcessEpersonService;

    @Autowired
    WorkflowProcessReferenceDocService workflowProcessReferenceDocService;

    @Autowired
    WorkflowProcessReferenceDocVersionService workflowProcessReferenceDocVersionService;


    @Autowired
    ItemConverter itemConverter;
    @Autowired
    DigitalSign digitalSign;


    boolean isdraftnotesin = false;

    /**
     * Method to upload a Bitstream to a Bundle with the given UUID in the URL. This will create a Bitstream with the
     * file provided in the request and attach this to the Item that matches the UUID in the URL.
     * This will only work for uploading one file, any extra files will silently be ignored
     *
     * @return The created BitstreamResource
     */


    // emudra APIS code start
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD}, value = "/getTokenRequest")
    public RequestData getRequestData(HttpServletRequest request, HttpServletResponse res) throws Exception {
        RequestData req = new RequestData();
        Request data = null;
        try {
            String emBridgeLicense = configurationService.getProperty("em.bridge.license");
            String emBridgeLogs = configurationService.getProperty("em.bridge.logs");
            emBridge bridge = new emBridge(emBridgeLicense, emBridgeLogs);
            ListTokenRequest listTokenRequest = new ListTokenRequest();
            listTokenRequest.setTokenStatus(Token_Status.CONNECTED);
            listTokenRequest.setTokenType(Token_Type.HARDWARE); // Token_Type.ALL for windows certificate
            data = bridge.encListToken(listTokenRequest);
            req.setEncryptedRequest(data.getEncryptedData());
            req.setEncryptionKeyId(data.getEncryptionKeyID());
            req.setStatus(data.getStatus());
            req.setErrorCode(data.getErrorCode());
            req.setErrorMsg(data.getErrorMsg());
            return req;
        } catch (Exception e) {
            log.error("Errro in getRequestData"+e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = {RequestMethod.POST}, value = "/getTokenResponse")
    public ResponseDataListProviderToken getTokenResponse(HttpServletRequest request, @RequestBody RequestData data) throws Exception {
        try {
            String emBridgeLicense = configurationService.getProperty("em.bridge.license");
            String emBridgeLogs = configurationService.getProperty("em.bridge.logs");
            emBridge bridge = new emBridge(emBridgeLicense, emBridgeLogs);

            ResponseDataListProviderToken responseDataListProviderToken = bridge.decListToken(data.getEncryptedRequest());
            return responseDataListProviderToken;
        } catch (Exception e) {
            log.error("Errro in getTokenResponse"+e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = {RequestMethod.POST}, value = "/getCertificateRequestByKeyStoreDisplayName")
    public RequestData getCertificateRequestByKeyStoreDisplayName(@Parameter(value = "keyStoreDisplayName", required = true) String keyStoreDisplayName) throws Exception {
        System.out.println(":::::::::getCertificateRequest::::::::::::");
        RequestData req = new RequestData();
        try {
            String emBridgeLicense = configurationService.getProperty("em.bridge.license");
            String emBridgeLogs = configurationService.getProperty("em.bridge.logs");
            emBridge bridge = new emBridge(emBridgeLicense, emBridgeLogs);
            ListCertificateRequest listCertRequest = new ListCertificateRequest();
            // CertificateFilter certFilter = new CertificateFilter();
            // certFilter.setIsNotExpired(true);
            if (keyStoreDisplayName != null) {
                listCertRequest.setKeyStoreDisplayName(keyStoreDisplayName);// Microsoft Windows																					// Store//ePass V																							// 2.0-22A4437C00498004(ePass2003)
            }
            Request data = bridge.encListCertificate(listCertRequest);
            req.setEncryptedRequest(data.getEncryptedData());
            req.setEncryptionKeyId(data.getEncryptionKeyID());
            req.setErrorMsg(data.getErrorMsg());
            req.setStatus(data.getStatus());
            req.setErrorCode(data.getErrorCode());
            return req;
        } catch (Exception e) {
            log.error("Errror in getCertificateRequestByKeyStoreDisplayName"+e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = {RequestMethod.POST}, value = "/getCertificateResponse")
    public ResponseDataListPKCSCertificate getCertificateResponse(@RequestBody RequestData data) throws Exception {
        try {
            String emBridgeLicense = configurationService.getProperty("em.bridge.license");
            String emBridgeLogs = configurationService.getProperty("em.bridge.logs");
            emBridge bridge = new emBridge(emBridgeLicense, emBridgeLogs);
            ResponseDataListPKCSCertificate responseDataListPKCSCertificate = bridge.decListCertificate(data.getEncryptedRequest(), new CertificateFilter());
            return responseDataListPKCSCertificate;
        } catch (Exception e) {
            log.error("Errror in getCertificateResponse"+e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }


    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = {RequestMethod.POST}, value = "/pdfSignRequest")
    public RequestData pdfSignRequest(@RequestBody EmudraRequestData request, HttpServletRequest requests) throws Exception {
        Context context = ContextUtil.obtainContext(requests);
        context.turnOffAuthorisationSystem();
        RequestData req = new RequestData();
        try {
            this.isdraftnotesin = false;
            InputStream fileInputa = null;
            System.out.println("::::::pdfSign::::");
            RequestData rest = request.getRequstedData();
            final String TEMP_DIRECTORY = getFolderTmp("UnSign");

            String emBridgeLicense = configurationService.getProperty("em.bridge.license");
            String emBridgeLogs = configurationService.getProperty("em.bridge.logs");
            emBridge bridge = new emBridge(emBridgeLicense, emBridgeLogs);
            // Initiate emBridge Object
            List<emBridgeSignerInput> inputs = new ArrayList<>();
            // Get All pdf files from provided folder
            Iterator<Path> paths = null;
            String s = null;
            System.out.println("doc id" + rest.getDocumentuuid());
            WorkflowProcessReferenceDoc workflowProcessReferenceDoc = workflowProcessReferenceDocService.find(context, UUID.fromString(rest.getDocumentuuid()));
            WorkflowProcess workflowProcess = workflowProcessReferenceDoc.getWorkflowProcess();

            if (workflowProcessReferenceDoc != null) {
                WorkflowProcessReferenceDocVersion workflowProcessReferenceDocVersion = workflowProcessReferenceDoc.getWorkflowProcessReferenceDocVersion().stream().filter(i -> i.getIsactive()).findFirst().get();
                if (workflowProcessReferenceDocVersion != null && workflowProcessReferenceDocVersion.getEditortext() != null) {
                    Random random = new Random();
                    // Generate a random 4-digit number
                    int randomNumber = random.nextInt(9000) + 1000;
                    File tempFile1html = new File(TEMP_DIRECTORY, "approvaldraft_" + randomNumber + ".pdf");
                    if (!tempFile1html.exists()) {
                        try {
                            tempFile1html.createNewFile();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    System.out.println("TEMP_DIRECTORY1::" + TEMP_DIRECTORY);
                    String type = getDrftType(workflowProcess);
                    if (type != null&&!type.isEmpty()) {
                        System.out.println("type::::" + type);
                        String htmlformatepath = pdfOprationServer.getHtmlTamplateByType(type);
                        Map<String, String> map = pdfOprationServer.getMAPByType(type, context, workflowProcessReferenceDocVersion.getEditortext(), workflowProcess);
                        fileInputa = pdfgenrator(context, workflowProcess, map, htmlformatepath, tempFile1html);
                    } else {
                        fileInputa = createFinalDraftDoc(context, workflowProcessReferenceDocVersion, tempFile1html, workflowProcess);
                    }
                    paths = Files.newDirectoryStream(Paths.get(TEMP_DIRECTORY), path -> path.toString().endsWith(".pdf")).iterator();
                }
            }
            String commonName = "";
            if (request.getRequstedData() != null && request.getRequstedData().getCommonName() != null) {
                commonName = request.getRequstedData().getCommonName();
            } else {
                commonName = context.getCurrentUser().getFullName();
            }
            String designation = "";
            Optional<EpersonToEpersonMapping> maps = context.getCurrentUser().getEpersonToEpersonMappings().stream().filter(d -> d.getIsactive() == true).findFirst();
            if (maps.isPresent()) {
                if (maps.get().getEpersonmapping().getDesignation().getPrimaryvalue() != null) {
                    designation = maps.get().getEpersonmapping().getDesignation().getPrimaryvalue();
                } else {
                    designation = "-";
                }
            }

            // Read all pdf files and create base64
            while (paths.hasNext()) {
                try {
                    Path path = paths.next();
                    File pdf = path.toFile();
                    InputStream pdfFile = new FileInputStream(pdf);
                    byte[] pdfBytes = new byte[(int) pdf.length()];
                    pdfFile.read(pdfBytes, 0, pdfBytes.length);
                    pdfFile.close();
                    String pdfStr = Base64.encodeBase64String(pdfBytes);
                    System.out.println("Coordinates.BottomRight::::;;" + Coordinates.BottomMiddle);

                    //  emBridgeSignerInput input = new emBridgeSignerInput(pdfStr, pdf.getName(), "Pune", "Distribution",context.getCurrentUser().getFullName(), true, PageTobeSigned.First, Coordinates.BottomRight, "", false);
//            String Cordinate=PDFTextSearch.getCordinate(path.toAbsolutePath().toString(),"Signature_1");
//            System.out.println("cordinate ::::"+Cordinate);
//            String c="1-"+Cordinate;
//
                    int pageno = PDFTextSearch.getPageNumberByText(1, path.toAbsolutePath().toString());
                    System.out.println("page number :::" + pageno);
                    String Cordinate = "408,410,574,456";
                    if (pageno < 0) {
                        pageno = 0;
                    } else {
                        Cordinate = PDFTextSearch.getCordinate(path.toAbsolutePath().toString(), "Digital_Signature", pageno);
                    }
                    int p = pageno + 1;
                    String c = p + "-" + Cordinate;

                    System.out.println("Cordinate.....2" + Cordinate);
                    String location = (request.getRequstedData().getLocation() != null ? request.getRequstedData().getLocation() : "-");
                    // String reason = (request.getRequstedData().getReason() != null ? request.getRequstedData().getReason() : "-");
                    emBridgeSignerInput emSignerInputforMultipleSignature4 = new emBridgeSignerInput(pdfStr, "4", "", "", "", true,
                            c, "Name : " + commonName + "\nDesignation:" + designation + "\nLocation:" + location + ".\nDate :" + getDateCurrentDate() + "", false);

                    inputs.add(emSignerInputforMultipleSignature4);
                } catch (IOException ioEx) {

                    ioEx.printStackTrace();
                    System.err.println("I/O error processing file: " + ioEx.getMessage());
                    log.error("Errror in pdfSignRequest"+ioEx.getMessage());

                } catch (RuntimeException runtimeEx) {
                    runtimeEx.printStackTrace();
                    System.err.println("Runtime error with file: => " + runtimeEx.getMessage());
                    log.error("Errror in pdfSignRequest"+runtimeEx.getMessage());

                }catch (Exception e){
                    e.printStackTrace();
                    System.err.println("Exception: => " + e.getMessage());
                    log.error("Errror in pdfSignRequest"+e.getMessage());
                }
            }
            // Initiate PKCSBulkPdfHashSignRequest Object
            PKCSBulkPdfHashSignRequest pKCSBulkPdfHashSignRequest = new PKCSBulkPdfHashSignRequest();
            // Add data to PKCSBulkPdfHashSignRequest's Object
            pKCSBulkPdfHashSignRequest.setBulkInput(inputs);
            pKCSBulkPdfHashSignRequest.setTempFolder(TEMP_DIRECTORY);
            if (rest.getKeyStoreDisplayName() != null) {
                pKCSBulkPdfHashSignRequest.setKeyStoreDisplayName(rest.getKeyStoreDisplayName()); // token name
            }
            if (rest.getKeyStorePassphrase() != null) {
                pKCSBulkPdfHashSignRequest.setKeyStorePassphrase(rest.getKeyStorePassphrase());// token's password
            }
            if (rest.getKeyId() != null) {
                pKCSBulkPdfHashSignRequest.setKeyId(rest.getKeyId());// certificates id
            }
            //vipul
            Request bulkPKCSSignRequest = bridge.encPKCSBulkSign(pKCSBulkPdfHashSignRequest);
            String r1 = bulkPKCSSignRequest.getEncryptedData();
            String r2 = bulkPKCSSignRequest.getEncryptionKeyID();
            req.setEncryptedRequest(r1);
            req.setEncryptionKeyId(r2);
            req.setErrorMsg(bulkPKCSSignRequest.getErrorMsg());
            req.setStatus(bulkPKCSSignRequest.getStatus());
            req.setErrorCode(bulkPKCSSignRequest.getErrorCode());
            req.setTempfilepath(bulkPKCSSignRequest.getTempFilePath());
            req.setTempfolder(TEMP_DIRECTORY);
            this.isdraftnotesin = true;
            return req;
        }catch (FieldBlankOrNullException w){
            w.printStackTrace();
            System.err.println("I/O error processing file: " + w.getMessage());
            log.error("Errror in pdfSignRequest"+w.getMessage());
            throw  new FieldBlankOrNullException("PDF conversion failed. This might be due to unsupported content or a system error. Please check your input and try again.");
        } catch (Exception e) {
            log.error("Errror in pdfSignRequest"+e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public String getDrftType(WorkflowProcess workflowProcess) {
        if (workflowProcess.getWorkFlowProcessDraftDetails() != null) {
            if (workflowProcess.getWorkFlowProcessDraftDetails().getDrafttype() != null && workflowProcess.getWorkFlowProcessDraftDetails().getDrafttype().getPrimaryvalue() != null) {
                return workflowProcess.getWorkFlowProcessDraftDetails().getDrafttype().getPrimaryvalue();
            }
        }
        return null;
    }

    public InputStream pdfgenrator(Context context, WorkflowProcess workflowProcess, Map<String, String> map, String templateFullpath, File tempFile1html) throws IOException, SQLException, AuthorizeException, FieldBlankOrNullException {
        InputStream inputStream = null;
        String htmlString = "";
        try {
            PDfObject pDfObject = new PDfObject(new String());
            String htmlContent = PdfUtils.readHtmlFile(templateFullpath);
            String replacedHtml = PdfUtils.replacePlaceholders(htmlContent, map);
            pDfObject.setHtmlContent(replacedHtml);
            htmlString = replacedHtml;
            // inputStream =pdfOprationServer.fetchpdfFromhtml(pDfObject);
            // System.out.println(":::replacedHtml:::"+replacedHtml);

        } catch (Exception e) {
            e.printStackTrace();
        }
        final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
        File replyDraft = new File(TEMP_DIRECTORY, "ReplyDraft.pdf");
        if (!replyDraft.exists()) {
            try {
                replyDraft.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        FileOutputStream files = new FileOutputStream(new File(replyDraft.getAbsolutePath()));
        //System.out.println("HTML:::" + htmlString);
        //int result = PdfUtils.HtmlconvertToPdf(sb.toString(), files);
        int ii = jbpmServer.htmltopdf(htmlString, files);
        if(ii==0){
            throw  new FieldBlankOrNullException("PDF conversion failed. This might be due to unsupported content or a system error. Please check your input and try again.");
        }
        System.out.println("HTML CONVERT DONE::::::::::::::: :" + replyDraft.getAbsolutePath());
        FileInputStream draftFile = new FileInputStream(new File(replyDraft.getAbsolutePath()));
        FileInputStream marge = getMargedDoc(context, workflowProcess, draftFile, tempFile1html);
        return marge;

    }

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = {RequestMethod.POST}, value = "/pdfSignRequestNote")
    public RequestData pdfSignRequestNote(@RequestBody EmudraRequestData request, HttpServletRequest requests) throws Exception {
        Context context = ContextUtil.obtainContext(requests);
        context.turnOffAuthorisationSystem();
        RequestData req = new RequestData();
        try {
            InputStream fileInputa = null;
            System.out.println("::::::pdfSign::::");
            RequestData rest = request.getRequstedData();
            final String TEMP_DIRECTORY = getFolderTmp("UnSign");
            String emBridgeLicense = configurationService.getProperty("em.bridge.license");
            String emBridgeLogs = configurationService.getProperty("em.bridge.logs");
            emBridge bridge = new emBridge(emBridgeLicense, emBridgeLogs);
            // Initiate emBridge Object
            List<emBridgeSignerInput> inputs = new ArrayList<>();
            // Get All pdf files from provided folder
            Iterator<Path> paths = null;
            String pathe = null;
            Integer index = 0;
            System.out.println("doc id" + rest.getDocumentuuid());
            WorkflowProcessReferenceDoc workflowProcessReferenceDoc = workflowProcessReferenceDocService.find(context, UUID.fromString(rest.getDocumentuuid()));
            if (workflowProcessReferenceDoc != null) {
                WorkflowProcess workFlowProcess = workflowProcessReferenceDoc.getWorkflowProcess();
                if (workFlowProcess != null) {
                    Optional<Integer> countFalseIssignnote = workFlowProcess.getWorkflowProcessEpeople().stream()
                            .filter(d -> d.getePerson().getID().toString().equalsIgnoreCase(context.getCurrentUser().getID().toString())).map(d -> d.getIndex()).findFirst();
                    if (countFalseIssignnote.isPresent()) {
                        index = countFalseIssignnote.get();
                        index = index + 1;
                        System.out.println(":::::::::::::index::::::::" + index);
                    }
                }
                System.out.println("TEMP_DIRECTORY1::" + TEMP_DIRECTORY);
                fileInputa = bitstreamService.retrieve(context, workflowProcessReferenceDoc.getBitstream());
                byte[] pdfBytes1 = inputStreamToByteArray(fileInputa);
                pathe = dsBitStoreService.getFilePath(workflowProcessReferenceDoc.getBitstream());
                String pdfStr = Base64.encodeBase64String(pdfBytes1);
                int pageno = PDFTextSearch.getPageNumberByText(1, pathe);
                System.out.println("page number :::" + pageno);
                String cordinate = "408,410,574,456";
                if (pageno < 0) {
                    pageno = 0;
                } else {
                    cordinate = PDFTextSearch.getCordinate(pathe, "Signature_", pageno);
                }
                int p = pageno + 1;
                String c = p + "-" + cordinate;
                System.out.println("cordinate ::::" + c);
                String commonName="";
                if(request.getRequstedData()!=null&&request.getRequstedData().getCommonName()!=null){
                    commonName= request.getRequstedData().getCommonName();
                }else{
                    commonName=context.getCurrentUser().getFullName();
                }

                String designation = "";
                Optional<EpersonToEpersonMapping> maps = context.getCurrentUser().getEpersonToEpersonMappings().stream().filter(d -> d.getIsactive() == true).findFirst();
                if (maps.isPresent()) {
                    if (maps.get().getEpersonmapping().getDesignation().getPrimaryvalue() != null) {
                        designation = maps.get().getEpersonmapping().getDesignation().getPrimaryvalue();
                    } else {
                        designation = "-";
                    }
                }

                String location = (request.getRequstedData().getLocation() != null ? request.getRequstedData().getLocation() : "-");
                //  String reason = (request.getRequstedData().getReason() != null ? request.getRequstedData().getReason() : "-");
//                emBridgeSignerInput emSignerInputforMultipleSignature4 = new emBridgeSignerInput(pdfStr, "4", "", "", "", true,
//                        c, "Name:" + commonName + "\nLocation:" + location + "\nReason:" + reason + "", false);
//
                emBridgeSignerInput emSignerInputforMultipleSignature4 = new emBridgeSignerInput(pdfStr, "4", "", "", "", true,
                        c, "Name : " + commonName + "\nDesignation:" + designation + "\nLocation:" + location + ".\nDate :" + getDateCurrentDate() + "", false);


                inputs.add(emSignerInputforMultipleSignature4);
                System.out.println("path:>>>>>>>:" + pathe);
            }
            PKCSBulkPdfHashSignRequest pKCSBulkPdfHashSignRequest = new PKCSBulkPdfHashSignRequest();
            pKCSBulkPdfHashSignRequest.setBulkInput(inputs);
            pKCSBulkPdfHashSignRequest.setTempFolder(TEMP_DIRECTORY);
            if (rest.getKeyStoreDisplayName() != null) {
                pKCSBulkPdfHashSignRequest.setKeyStoreDisplayName(rest.getKeyStoreDisplayName()); // token name
            } else {
                throw new RuntimeException("KeyStore Display Name Not FOUND!; ");
            }
            if (rest.getKeyStorePassphrase() != null) {
                pKCSBulkPdfHashSignRequest.setKeyStorePassphrase(rest.getKeyStorePassphrase());// token's password
            } else {
                throw new RuntimeException("Key StorePassphrase Not FOUND!; ");
            }
            if (rest.getKeyId() != null) {
                pKCSBulkPdfHashSignRequest.setKeyId(rest.getKeyId());// certificates id
            } else {
                throw new RuntimeException("Key KeyId Not FOUND!; ");
            }
            Request bulkPKCSSignRequest = bridge.encPKCSBulkSign(pKCSBulkPdfHashSignRequest);
            String r1 = bulkPKCSSignRequest.getEncryptedData();
            String r2 = bulkPKCSSignRequest.getEncryptionKeyID();
            req.setErrorMsg(bulkPKCSSignRequest.getErrorMsg());
            req.setErrorCode(bulkPKCSSignRequest.getErrorCode());
            req.setStatus(bulkPKCSSignRequest.getStatus());
            req.setEncryptedRequest(r1);
            req.setEncryptionKeyId(r2);
            req.setTempfilepath(bulkPKCSSignRequest.getTempFilePath());
            req.setTempfolder(TEMP_DIRECTORY);
            //this.isdraftnotesin=true;
            return req;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("" + e.getMessage());
        }
    }


    public static byte[] inputStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024]; // Buffer size
        int bytesRead;

        // Read bytes from InputStream into ByteArrayOutputStream
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }

        // Convert ByteArrayOutputStream to byte array
        return byteArrayOutputStream.toByteArray();
    }

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = {RequestMethod.POST}, value = "/pdfSignResponse")
    public RequestData pdfSignResponse(@RequestBody RequestData request, HttpServletRequest requests) throws Exception {
        Context context = ContextUtil.obtainContext(requests);
        context.turnOffAuthorisationSystem();
        try {
            RequestData req = new RequestData();
            String emBridgeLicense = configurationService.getProperty("em.bridge.license");
            String emBridgeLogs = configurationService.getProperty("em.bridge.logs");
            emBridge bridge = new emBridge(emBridgeLicense, emBridgeLogs);
            ResponseDataPKCSBulkSign apiResponse = bridge.decPKCSBulkSign(request.getEncryptedRequest(), request.getTempfilepath());
            // //Genearate pdf file from Base64 and delete temp file
            Bitstream sign = generatePDFFile(context, apiResponse, request);

            if (sign != null) {
                req.setBitstreampid(sign.getID().toString());
            }
            req.setErrorMsg(apiResponse.getErrorMsg());
            req.setStatus(apiResponse.getStatus());
            req.setErrorCode(apiResponse.getErrorCode());
            return req;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    //    public static String getFolderTmp(String foldername){
//        final String TEMP_DIRECTORY1 = System.getProperty("java.io.tmpdir");
//        Date now = new Date(System.currentTimeMillis() + 3 * 60 * 1000);
//        String sdf = new SimpleDateFormat("ddMMMyyyyHHmmss").format(now);
//        File theDir = new File(TEMP_DIRECTORY1+"\\"+foldername+"_"+sdf);
//        if (!theDir.exists()){
//            theDir.mkdirs();
//        }
//        return theDir.getAbsoluteFile().getAbsolutePath();
//    }


    public static String getFolderTmp(String folderName) {
        final String tempDirectory = System.getProperty("java.io.tmpdir");
        String timestamp = java.time.format.DateTimeFormatter.ofPattern("ddMMMyyyyHHmmss")
                .format(java.time.LocalDateTime.now().plusMinutes(3));
        File directory = new File(tempDirectory + File.separator + folderName + "_" + timestamp);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new RuntimeException("Failed to create temporary directory: " + directory.getAbsolutePath());
        }
        return directory.getAbsolutePath();
    }


    public Bitstream generatePDFFile(Context context, ResponseDataPKCSBulkSign serviceResponse, RequestData request) throws Exception {
        Bitstream bitstreamsing = null;
        try {
            String filePath = request.getTempfilepath();
            String folderpath = request.getTempfolder();
            System.out.println("folder " + filePath);
            System.out.println("filePath " + folderpath);
            WorkflowProcess workflowProcess = null;
            WorkflowProcessReferenceDoc wdoc=null;
            String name = "";
            if (request.getDocumentuuid() != null) {
                wdoc = workflowProcessReferenceDocService.find(context, UUID.fromString(request.getDocumentuuid()));
                if (wdoc != null) {
                    int i = wdoc.getIndex() != null ? wdoc.getIndex() : 1;
                    name = "document" + i;
                } else {
                    name = "document";
                }
            }
            File tempFile = new File(filePath);
            SimpleDateFormat tsFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            Date now = new Date(System.currentTimeMillis() + 3 * 60 * 1000);
            String timeStamp = tsFormat.format(now);
            System.out.println("time Stamp" + timeStamp);
            String sdf = new SimpleDateFormat("ddMMMyyyyHHmmss").format(now);
            final String TEMP_DIRECTORY1 = getFolderTmp("Sign");
            int count = 1;
            for (BulkSignOutput doc : serviceResponse.getBulkSignItems()) {
                Random random = new Random();
                // Generate a random 4-digit number
                int randomNumber = random.nextInt(9000) + 1000;
                byte[] signedDocBytes = Base64.decodeBase64(doc.getSignedData());
                File file = new File(TEMP_DIRECTORY1, "signed_" + name + "_" + randomNumber + ".pdf");
                System.out.println("sing doc" + file.getAbsolutePath());
                OutputStream os = new FileOutputStream(file);
                os.write(signedDocBytes);
                os.close();
                count++;
                FileInputStream outputfile = new FileInputStream(new File(file.getAbsolutePath()));
                System.out.println(" sing doc file.getAbsolutePath() " + file.getAbsolutePath());
                Bitstream bitstream = bundleRestRepository.processBitstreamCreationWithoutBundle(context, outputfile, "", file.getName());
                bitstreamsing = bitstream;

                WorkflowProcessReferenceDoc workflowProcessReferenceDoc = workflowProcessReferenceDocService.find(context, UUID.fromString(request.getDocumentuuid()));
                workflowProcess = workflowProcessReferenceDoc.getWorkflowProcess();
                if (workflowProcessReferenceDoc != null) {
                    Optional<WorkflowProcessReferenceDocVersion> workflowProcessReferenceDocVersion = workflowProcessReferenceDoc.getWorkflowProcessReferenceDocVersion().stream().filter(d -> d != null).filter(i -> i.getIsactive()).findFirst();
                    if (workflowProcessReferenceDocVersion.isPresent()) {
                        WorkflowProcessReferenceDocVersion v = workflowProcessReferenceDocVersionService.find(context, workflowProcessReferenceDocVersion.get().getID());
                        if (v != null) {
                            if (bitstream != null) {
                                v.setBitstream(bitstream);
                            }
                            v.setIssign(true);
                            workflowProcessReferenceDocVersionService.update(context, v);
                            if (bitstream != null) {
                                workflowProcessReferenceDoc.setBitstream(bitstream);
                            }
                            System.out.println("bitstreampdfsing::" + bitstream.getID());
                        }
                    }
                    System.out.println("bitstreampdfsing::" + bitstream.getID());

                    workflowProcessReferenceDoc.setBitstream(bitstreamsing);
                    workflowProcessReferenceDoc.setIssignature(true);
                    workflowProcessReferenceDocService.update(context, workflowProcessReferenceDoc);
                }
                if (workflowProcess != null && workflowProcess.getWorkFlowProcessDraftDetails() != null && workflowProcess.getWorkFlowProcessDraftDetails().getIssinglatter() == false) {
                    if (workflowProcess.getWorkflowStatus() != null && workflowProcess.getWorkflowStatus().getPrimaryvalue().equalsIgnoreCase("Close")) {
                        if (workflowProcess.getIsinternal() && workflowProcess.getWorkflowProcessSenderDiaryEpeople() != null) {
                            if (workflowProcess.getWorkflowType() != null && workflowProcess.getWorkflowType().getPrimaryvalue().equalsIgnoreCase("Draft")) {
                                createTapal(context, workflowProcess);
                            }
                        } else {
                            WorkFlowProcessDraftDetails d = workFlowProcessDraftDetailsService.find(context, workflowProcess.getWorkFlowProcessDraftDetails().getID());
                            if (d != null) {
                                System.out.println("update done signflag::  ::::::::::::::::");
                                d.setIssinglatter(true);
                                workFlowProcessDraftDetailsService.update(context, d);
                            }
                            System.out.println("create tapal done after sign:");
                        }
                    }
                }

                if (workflowProcess != null && workflowProcess.getWorkFlowProcessDraftDetails() != null && workflowProcess.getWorkFlowProcessDraftDetails().getIssinglatter() == false) {
                    if (workflowProcess.getIsinternal() && workflowProcess.getWorkflowProcessSenderDiaryEpeople() != null) {
                        if (workflowProcess.getWorkflowType() != null && workflowProcess.getWorkflowType().getPrimaryvalue().equalsIgnoreCase("Inward")) {
                            createTapal(context, workflowProcess);
                            System.out.println("create tapal done after sign:");
                        }
                    } else {
                        WorkFlowProcessDraftDetails d = workFlowProcessDraftDetailsService.find(context, workflowProcess.getWorkFlowProcessDraftDetails().getID());
                        if (d != null) {
                            System.out.println("update done signflag::::   ::::::::::::::");
                            d.setIssinglatter(true);
                            workFlowProcessDraftDetailsService.update(context, d);
                        }

                    }
                }
                if(wdoc.getDrafttype()!=null&&wdoc.getDrafttype().getPrimaryvalue()!=null&&wdoc.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Reply Note")||wdoc.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Reply Tapal")) {
                    storeWorkFlowHistory(context, workflowProcess);
                }
                context.commit();
                //  file.delete();
            }
            tempFile.delete();
            deleteFolderTmp(folderpath);
            return bitstreamsing;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("" + e.getMessage());
        }
    }
    public void storeWorkFlowHistory(Context context, WorkflowProcess workflowProcess) throws Exception {
        System.out.println("::::::IN :store WorkFlow History:::::Document signed::: ");
        WorkFlowProcessHistory workFlowAction = new WorkFlowProcessHistory();
        WorkFlowProcessMaster workFlowProcessMaster = WorkFlowAction.MASTER.getMaster(context);
        WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, WorkFlowAction.COMPLETE.getAction(), workFlowProcessMaster);
        workFlowAction.setActionDate(new Date());
        workFlowAction.setAction(workFlowProcessMasterValue);
        workFlowAction.setWorkflowProcess(workflowProcess);
        workFlowAction.setSentbyname(context.getCurrentUser().getFullName());
        workFlowAction.setSenttoname(context.getCurrentUser().getFullName());
        workFlowAction.setComment("Draft Document signed by " +context.getCurrentUser().getFullName()+".");
        workFlowProcessHistoryService.create(context, workFlowAction);
        System.out.println("::::::::OUT :store WorkFlow History:::::Document signed::: done::: ");
    }

    public static void deleteFolderTmp(String s) {
        File theDir = new File(s);
        if (theDir.exists()) {
            theDir.delete();
            System.out.println("deleted " + theDir.getAbsolutePath());
        }
        System.out.println("already deleted");
    }

// emudra APIS code end


    public FileInputStream createFinalDraftDoc(Context context, WorkflowProcessReferenceDocVersion workflowProcessReferenceDoc, File tempFile1html, WorkflowProcess workflowProcess) throws Exception {
        String subject = getSubjectDraft(workflowProcess);
        boolean isTextEditorFlow = false;
        //System.out.println("start.......sing");
        StringBuffer sb = new StringBuffer("<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head><style>@page{size:A4;margin: 0;}</style>\n" +
                "<title>Note</title>\n" +
                "</head>\n" +
                "<body style=\"padding-right: 20px;padding-left: 20px;\">");

        String logopath = configurationService.getProperty("pcmc.acknowledgement.logo");
        String base64Image = java.util.Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(logopath)));
        if (base64Image != null) {
            sb.append("<img src=\"data:image/png;base64," + base64Image + "\" style=\"margin:32px; height:80px;\">");
        }
        sb.append("<br><div style=\"float:right;padding-right:100px;margin-top: -100px;\">");
        sb.append("<p> <B>पिंपरी चिंचवड महानगरपालिका,<br>पिंपरी<br></B></p> </div> <br>");
        sb.append("<div>");
//        Optional<WorkflowProcessSenderDiary> list=workflowProcess.getWorkflowProcessSenderDiaries().stream().filter(d->d.getStatus()==2).findFirst();
//        if(workflowProcess!=null&&workflowProcess.getWorkflowProcessSenderDiaries()!=null&&workflowProcess.getWorkflowProcessSenderDiaries().size()!=0&&list.isPresent()) {
//            System.out.println("in diry::::"+workflowProcess.getWorkflowProcessSenderDiaries().size());
//            sb.append("<p><b>To.</b>");
//           for (WorkflowProcessSenderDiary to:workflowProcess.getWorkflowProcessSenderDiaries()) {
//                if(to.getStatus()==2) {
//                    System.out.println(" :::::::::::");
//                    if(to.getSendername()!=null) {
//                        sb.append("<br><b>" + to.getSendername() + ",</b>\n");
//                    }
//                    if(to.getAddress()!=null&&!to.getAddress().isEmpty()) {
//                        sb.append("<br>" + to.getAddress() + ",\n");
//                    } if(to.getCity()!=null&&!to.getCity().isEmpty()) {
//                        sb.append("<br>" + to.getCity() + ",\n");
//                    } if(to.getState()!=null&&!to.getState().isEmpty()) {
//                        sb.append("<br>" + to.getState() + ",\n");
//                    } if(to.getPincode()!=null&&!to.getPincode().isEmpty()) {
//                        sb.append("<br>" + to.getPincode() + ",\n");
//                    } if(to.getEmail()!=null&&!to.getEmail().isEmpty()) {
//                        sb.append("<br>" + to.getEmail() + ",\n");
//                    }if(to.getContactNumber()!=null&&!to.getContactNumber().isEmpty()) {
//                        sb.append("<br>" + to.getContactNumber() + "\n");
//                    }
//                     sb.append("</p> <br>");
//                }
//            }
//        }else{
//            if(workflowProcess.getIsinternal()&& workflowProcess.getWorkflowProcessSenderDiaryEpeople()!=null){
//                sb.append("<p><b>To.</b>");
//                for (WorkflowProcessSenderDiaryEperson to:workflowProcess.getWorkflowProcessSenderDiaryEpeople()) {
//                        if (to.getUsertype() != null && to.getUsertype().getPrimaryvalue().equalsIgnoreCase("To")) {
//                            if (to.getePerson() != null && to.getePerson().getFullName() != null) {
//                                sb.append("<br><b>" + to.getePerson().getFullName() + ",</b>\n");
//                            }
//                            if (to.getePerson().getEmail() != null) {
//                                sb.append("<br>" + to.getePerson().getEmail() + ",\n");
//                            }
//                            if (to.getePerson().getDesignation() != null && to.getePerson().getDesignation().getPrimaryvalue() != null) {
//                                sb.append("<br>" + to.getePerson().getDesignation().getPrimaryvalue() + ",\n");
//                            }
//                            sb.append("</p> <br>");
//                        }
//                }
//            }
//        }
//        sb.append("</div>");
//        sb.append("<p><b>Subject: "+subject+"</b></p>");
//


        if (workflowProcessReferenceDoc.getEditortext() != null) {
            isTextEditorFlow = true;
            sb.append("<div>" + workflowProcessReferenceDoc.getEditortext() + "</div>");
        }

        String designation = "";
        Optional<EpersonToEpersonMapping> map = context.getCurrentUser().getEpersonToEpersonMappings().stream().filter(d -> d.getIsactive() == true).findFirst();
        if (map.isPresent()) {
            designation = map.get().getEpersonmapping().getDesignation().getPrimaryvalue();
        }
//        sb.append("<br>Yours sincerely,<br><p>\n" +
//                "<b>"+context.getCurrentUser().getFullName()+"),");
//        if (designation!=null) {
//            sb.append("<br>" + designation + "</p>");
//        }
        sb.append("<br><br><br><div style=\" float: right; width:30%;\"><p> <B>Digital_Signature</B> </p><B><span></div>");
        sb.append("</body></html>");
        if (isTextEditorFlow) {
            System.out.println("::::::::::IN isTextEditorFlow :::::::::");
            final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
            File replyDraft = new File(TEMP_DIRECTORY, "ReplyDraft.pdf");
            if (!replyDraft.exists()) {
                try {
                    replyDraft.createNewFile();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            FileOutputStream files = new FileOutputStream(new File(replyDraft.getAbsolutePath()));
            //System.out.println("HTML:::" + sb.toString());
            //int result = PdfUtils.HtmlconvertToPdf(sb.toString(), files);
            int ii = jbpmServer.htmltopdf(sb.toString(), files);
            if(ii==0){
                throw  new FieldBlankOrNullException("PDF conversion failed. This might be due to unsupported content or a system error. Please check your input and try again.");
            }
            System.out.println("HTML CONVERT DONE::::::::::::::: :" + replyDraft.getAbsolutePath());

            FileInputStream draftFile = new FileInputStream(new File(replyDraft.getAbsolutePath()));
            FileInputStream marge = getMargedDoc(context, workflowProcess, draftFile, tempFile1html);
            return marge;
        }
        return null;
    }


    public UUID getInwardDocument(Context context, WorkflowProcess workflowProcess) {
        WorkflowProcessReferenceDoc draftsigndocument = null;

        try {
            Optional<WorkflowProcessReferenceDoc> signdoc = null;
            if (workflowProcess.getWorkflowType() != null && workflowProcess.getWorkflowType().getPrimaryvalue().equalsIgnoreCase("Inward")) {
                System.out.println("in inward ::::::::::::::::doc");
                signdoc = workflowProcess.getWorkflowProcessReferenceDocs()
                        .stream().filter(d -> d != null).filter(d -> d.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Reply Tapal"))
                        .findFirst();
            } else {
                signdoc = workflowProcess.getWorkflowProcessReferenceDocs()
                        .stream().filter(d -> d != null).filter(d -> d.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Reply Note"))
                        .findFirst();
            }
            if (signdoc.isPresent()) {
                WorkflowProcessReferenceDoc doc = new WorkflowProcessReferenceDoc();
                WorkflowProcessReferenceDocRest rest = workflowProcessReferenceDocConverter.convert(signdoc.get(), utils.obtainProjection());
                doc = workflowProcessReferenceDocConverter.convert(rest, context);

                WorkFlowProcessMaster workFlowProcessMaster = workFlowProcessMasterService.findByName(context, "Draft Type");
                if (workFlowProcessMaster != null) {
                    WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, "Inward", workFlowProcessMaster);
                    if (workFlowProcessMasterValue != null) {
                        doc.setDrafttype(workFlowProcessMasterValue);
                    }
                }
                WorkFlowProcessMaster doctype = workFlowProcessMasterService.findByName(context, "Document Type");
                if (doctype != null) {
                    WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, "Letter", doctype);
                    if (workFlowProcessMasterValue != null) {
                        doc.setWorkFlowProcessReferenceDocType(workFlowProcessMasterValue);
                    }
                }
                if (signdoc.get().getBitstream() != null) {
                    InputStream pdfFileInputStream1 = bitstreamService.retrieve(context, signdoc.get().getBitstream());
                    if (pdfFileInputStream1 != null) {
                        doc.setPage(FileUtils.getPageCountInPDF(pdfFileInputStream1));
                    }
                }
                doc.setSubject(getSubjectDraft(workflowProcess));
                doc.setInitdate(new Date());
                doc.setReferenceNumber("-");
                draftsigndocument = workflowProcessReferenceDocService.create(context, doc);

            }
            return draftsigndocument.getID();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public UUID getByMasterNameAndValue(Context context, String mastername, String value) {
        UUID mastervalueuuid = null;
        try {
            WorkFlowProcessMaster dispatchMode = workFlowProcessMasterService.findByName(context, mastername);
            if (dispatchMode != null) {
                WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, value, dispatchMode);
                if (workFlowProcessMasterValue != null) {
                    mastervalueuuid = workFlowProcessMasterValue.getID();
                }
            }
            return mastervalueuuid;
        } catch (Exception e) {
            e.printStackTrace();
            return mastervalueuuid;
        }
    }


    public String getcategoruuids(Context context) {

        try {
            String categoruuids = "";
            List<Category> list = categoryService.getAll(context);
            Optional<String> categoruuid = list.stream().filter(d -> d != null).filter(d -> d.getCategoryname().equalsIgnoreCase("Others")).map(d -> d.getID().toString()).findFirst();
            if (categoruuid.isPresent()) {
                categoruuids = categoruuid.get();
            }
            return categoruuids;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getSubjectDraft(WorkflowProcess workflowProcess) {
        String subject = "";
        if (workflowProcess.getWorkFlowProcessDraftDetails() != null && workflowProcess.getWorkFlowProcessDraftDetails().getSubject() != null) {
            subject = workflowProcess.getWorkFlowProcessDraftDetails().getSubject();
        }
        return subject;
    }

    public void createTapal(Context context, WorkflowProcess workflowProcess) throws Exception {
        try {

            UUID draftsigndocument = getInwardDocument(context, workflowProcess);
            UUID dispatchModeuuid = getByMasterNameAndValue(context, "Dispatch Mode", "Electronic");
            UUID confidentialUuid = getByMasterNameAndValue(context, "Eligible for filing", "Yes");
            UUID documenttypeuuid = getByMasterNameAndValue(context, "Document Type", "Contracts");
            UUID languageMarathi = getByMasterNameAndValue(context, "Language", "Marathi");
            String categoruuids = getcategoruuids(context);
            String subject = getSubjectDraft(workflowProcess);
            String designation = "";
            Optional<EpersonToEpersonMapping> map = context.getCurrentUser().getEpersonToEpersonMappings().stream().filter(d -> d.getIsactive() == true).findFirst();
            if (map.isPresent()) {
                if (map.get().getEpersonmapping() != null) {
                    EpersonMapping s = map.get().getEpersonmapping();
                    designation = s.getDesignation().getPrimaryvalue();
                }
            }


            StringBuffer json = new StringBuffer("{")
                    .append("  \"Subject\": \"").append(escapeJson(subject)).append("\",\n")
                    .append("  \"workFlowProcessInwardDetailsRest\": {\n")
                    .append("    \"inwardNumber\": \"\",\n")
                    .append("    \"inwardDate\": \"").append(DateUtils.DateFormateMMDDYYYY(new Date())).append("\",\n")
                    .append("    \"filereferencenumber\": \"\",\n")
                    .append("    \"receivedDate\": \"").append(DateUtils.DateFormateMMDDYYYY(new Date())).append("\",\n")
                    .append("    \"latterDate\": \"").append(DateUtils.DateFormateMMDDYYYY(new Date())).append("\",\n")
                    .append("    \"vipRest\": \"\",\n")
                    .append("    \"vipnameRest\": \"\",\n")
                    .append("    \"subcategoryRest\": \"\",\n")
                    .append("    \"categoryRest\": \"").append(categoruuids).append("\",\n")
                    .append("    \"languageRest\": {\n")
                    .append("      \"uuid\": \"").append(languageMarathi).append("\"\n")
                    .append("    },\n")
                    .append("    \"inwardmodeRest\": {\n")
                    .append("      \"uuid\": \"").append("").append("\"\n")
                    .append("    },\n")
                    .append("    \"letterrefno\": \"\"\n")
                    .append("  },\n")
                    .append("  \"workFlowProcessDraftDetailsRest\": {\n")
                    .append("    \"drafttypeRest\": {\n")
                    .append("      \"uuid\": \"\"\n")
                    .append("    },\n")
                    .append("    \"draftnatureRest\": {\n")
                    .append("      \"uuid\": \"\"\n")
                    .append("    },\n")
                    .append("    \"confidentialRest\": {\n")
                    .append("      \"uuid\": \"").append(confidentialUuid).append("\"\n")
                    .append("    },\n")
                    .append("    \"replytypeRest\": {\n")
                    .append("      \"uuid\": \"\"\n")
                    .append("    },\n")
                    .append("    \"subject\": \"").append("").append("\",\n")
                    .append("    \"referencefilenumberRest\": \"\",\n")
                    .append("    \"documentsignatorRest\": {\n")
                    .append("      \"uuid\": \"\"\n")
                    .append("    }\n")
                    .append("  },\n")
                    .append("  \"workflowProcessSenderDiaryRests\": [\n")
                    .append("    {\n")
                    .append("      \"designation\": \"" + designation + "\",\n")
                    .append("      \"contactNumber\": \"\",\n")
                    .append("      \"email\": \"").append(context.getCurrentUser().getEmail()).append("\",\n")
                    .append("      \"organization\": \"\",\n")
                    .append("      \"address\": \"\",\n")
                    .append("      \"city\": \"\",\n")
                    .append("      \"state\": \"\",\n")
                    .append("      \"country\": \"\",\n")
                    .append("      \"sendername\": \"").append(context.getCurrentUser().getFullName()).append("\",\n")
                    .append("      \"fax\": \"\",\n")
                    .append("      \"landline\": \"\",\n")
                    .append("      \"pincode\": \"\",\n")
                    .append("      \"status\": \"1\"\n")
                    .append("    }\n")
                    .append("  ],\n")
                    .append("  \"dispatchModeRest\": {\n")
                    .append("    \"uuid\": \"").append(dispatchModeuuid).append("\"\n")
                    .append("  },\n")
                    .append("  \"workflowProcessReferenceDocRests\": [\n")
                    .append("    {\n")
                    .append("      \"uuid\": \"").append(draftsigndocument).append("\"\n")
                    .append("    }\n")
                    .append("  ],\n")
                    .append("  \"documenttypeRest\": {\n")
                    .append("    \"uuid\": \"").append(documenttypeuuid).append("\"\n")
                    .append("  },\n")
                    .append("  \"workflowProcessEpersonRests\": [\n");

            if (workflowProcess.getWorkflowProcessSenderDiaryEpeople() != null && !workflowProcess.getWorkflowProcessSenderDiaryEpeople().isEmpty()) {
                int i = 1;
                for (WorkflowProcessSenderDiaryEperson ep : workflowProcess.getWorkflowProcessSenderDiaryEpeople()) {
                    if (i > 1) {
                        json.append(",\n");
                    }
                    json.append("    {\n")
                            .append("      \"departmentRest\": {\n")
                            .append("        \"uuid\": \"").append(ep.getDepartment().getID()).append("\"\n")
                            .append("      },\n")
                            .append("      \"officeRest\": {\n")
                            .append("        \"uuid\": \"").append(ep.getOffice().getID()).append("\"\n")
                            .append("      },\n")
                            .append("      \"epersonToEpersonMappingRest\": {\n")
                            .append("        \"uuid\": \"").append(ep.getEpersontoepersonmapping().getID()).append("\"\n")
                            .append("      },\n")
                            .append("      \"ePersonRest\": {\n")
                            .append("        \"uuid\": \"").append(ep.getePerson().getID()).append("\"\n")
                            .append("      },\n")
                            .append("      \"index\": ").append(i).append("\n")
                            .append("    }\n");
                    i++;
                }
            }

            json.append("  ]\n")
                    .append("}");


            MultipartFile file = null;
            String workFlowProcessReststr = json.toString();
            System.out.println("json :::" + json.toString());
            workflowProcessInwardController.create(file, workFlowProcessReststr);

//update
            WorkFlowProcessDraftDetails d = workFlowProcessDraftDetailsService.find(context, workflowProcess.getWorkFlowProcessDraftDetails().getID());
            if (d != null) {
                System.out.println("update done sign flag::::::::::::::::::");
                d.setIssinglatter(true);
                workFlowProcessDraftDetailsService.update(context, d);
            }
            System.out.println("create tapal done after sign:");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("EROor" + e.getMessage());
        }
    }


    public String getDateCurrentDate() {
        String currentDateTime = "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            currentDateTime = sdf.format(new Date());
            System.out.println("Current Date and Time: " + currentDateTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentDateTime;
    }

    public static String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\t", "\\t")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    public FileInputStream getMargedDoc(Context context, WorkflowProcess workFlowProcess, InputStream draftInputStream, File output) throws SQLException, AuthorizeException, IOException {
        InputStream replyTapalInputstream = null;
        List<InputStream> inputreplyTapalReference = new ArrayList<>();
        List<Bitstream> bitstreamReferenceReply = null;
        Bitstream bitstreammarged = null;
        {
            System.out.println("in getMargedDoc :::::::::::::::::::");

            if (draftInputStream != null) {
                inputreplyTapalReference.add(draftInputStream);
            }
            if (workFlowProcess.getWorkflowType() != null && workFlowProcess.getWorkflowType().getPrimaryvalue().equalsIgnoreCase("Inward")) {
                bitstreamReferenceReply = workFlowProcess.getWorkflowProcessReferenceDocs().stream()
                        .filter(a -> a.getDrafttype() != null).filter(b -> b.getDrafttype().getPrimaryvalue() != null)
                        .filter(c -> c.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Reference Reply"))
                        .filter(d -> d.getBitstream() != null)
                        .map(d -> d.getBitstream()).collect(Collectors.toList());
            } else {
                bitstreamReferenceReply = workFlowProcess.getWorkflowProcessReferenceDocs().stream()
                        .filter(a -> a.getDrafttype() != null).filter(b -> b.getDrafttype().getPrimaryvalue() != null)
                        .filter(c -> c.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Reference Reply Note"))
                        .filter(d -> d.getBitstream() != null)
                        .map(d -> d.getBitstream()).collect(Collectors.toList());
            }

            if (bitstreamReferenceReply != null && !bitstreamReferenceReply.isEmpty()) {
                for (Bitstream bitstream : bitstreamReferenceReply) {
                    InputStream inputStream = bitstreamService.retrieve(context, bitstream);
                    inputreplyTapalReference.add(inputStream);
                }
            }

            OutputStream out = new FileOutputStream(new File(output.getAbsolutePath()));
            MargedDocUtils.mergePdfFiles(inputreplyTapalReference, out);
            FileInputStream outputfile = new FileInputStream(new File(output.getAbsolutePath()));
//            bitstreammarged = bundleRestRepository.processBitstreamCreationWithoutBundle(context, outputfile, "", output.getName());
//
//
//            doc.setBitstream(bitstreammarged);
//            workflowProcessReferenceDocService.update(context,doc);
            return outputfile;

        }
    }

}
