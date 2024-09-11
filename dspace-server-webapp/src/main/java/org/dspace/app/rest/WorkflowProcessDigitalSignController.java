/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import emh.Enum.Coordinates;
import emh.Enum.PageTobeSigned;
import emh.Enum.Token_Status;
import emh.Enum.Token_Type;
import emh.Model.RequestData.*;
import emh.Model.ResponseData.*;
import emh.emBridgeLib.emBridge;
import emh.emBridgeLib.emBridgeSignerInput;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.converter.ItemConverter;
import org.dspace.app.rest.converter.LoginCounterConverter;
import org.dspace.app.rest.converter.WorkflowProcessReferenceDocConverter;
import org.dspace.app.rest.jbpm.constant.JBPM;
import org.dspace.app.rest.jbpm.models.JBPMCallbackRequest;
import org.dspace.app.rest.model.*;
import org.dspace.app.rest.repository.BundleRestRepository;
import org.dspace.app.rest.utils.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.content.service.*;
import org.dspace.core.Context;
import org.dspace.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.*;

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
    public WorkflowProcessReferenceDocConverter workflowProcessReferenceDocConverter;
    @Autowired
    ConfigurationService configurationService;

    @Autowired
    private  BundleRestRepository bundleRestRepository;


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


    boolean isdraftnotesin=false;

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
    public RequestData getRequestData(HttpServletRequest request) throws Exception {
        RequestData req=new RequestData();
        Request data=null;
        try {
            String emBridgeLicense= configurationService.getProperty("em.bridge.license");
            String emBridgeLogs= configurationService.getProperty("em.bridge.logs");
            emBridge bridge = new emBridge(emBridgeLicense, emBridgeLogs);
            ListTokenRequest listTokenRequest = new ListTokenRequest();
            listTokenRequest.setTokenStatus(Token_Status.CONNECTED);
            listTokenRequest.setTokenType(Token_Type.HARDWARE); // Token_Type.ALL for windows certificate
             data = bridge.encListToken(listTokenRequest);
            req.setEncryptedRequest(data.getEncryptedData());
            req.setEncryptionKeyId(data.getEncryptionKeyID());
            return req;
        }catch (Exception e){
            e.printStackTrace();
           throw  new RuntimeException(e.getMessage());
        }
    }
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = {RequestMethod.POST}, value = "/getTokenResponse")
    public ResponseDataListProviderToken getTokenResponse(HttpServletRequest request,@RequestBody RequestData data) throws Exception {
        try {
            String emBridgeLicense= configurationService.getProperty("em.bridge.license");
            String emBridgeLogs= configurationService.getProperty("em.bridge.logs");
            emBridge bridge = new emBridge(emBridgeLicense, emBridgeLogs);
            ResponseDataListProviderToken responseDataListProviderToken = bridge.decListToken(data.getEncryptedRequest());
            return responseDataListProviderToken;
        }catch (Exception e){
            e.printStackTrace();
            throw  new RuntimeException(e.getMessage());
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
            return req;
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException(e.getMessage());
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
            e.printStackTrace();
            throw  new RuntimeException(e.getMessage());
        }
    }


    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = {RequestMethod.POST}, value = "/pdfSignRequest")
    public RequestData pdfSignRequest(@RequestBody EmudraRequestData request,HttpServletRequest requests) throws Exception {
        Context context = ContextUtil.obtainContext(requests);
        context.turnOffAuthorisationSystem();
        this.isdraftnotesin=false;
        InputStream fileInputa = null;
        System.out.println("::::::pdfSign::::");
        RequestData rest = request.getRequstedData();
        final String TEMP_DIRECTORY=getFolderTmp("UnSign");
        RequestData req = new RequestData();
        String emBridgeLicense = configurationService.getProperty("em.bridge.license");
        String emBridgeLogs = configurationService.getProperty("em.bridge.logs");
        emBridge bridge = new emBridge(emBridgeLicense, emBridgeLogs);
        // Initiate emBridge Object
        List<emBridgeSignerInput> inputs = new ArrayList<>();
        // Get All pdf files from provided folder
        Iterator<Path> paths=null;
        String s = null;
        System.out.println("doc id" + rest.getDocumentuuid());
        WorkflowProcessReferenceDoc workflowProcessReferenceDoc = workflowProcessReferenceDocService.find(context, UUID.fromString(rest.getDocumentuuid()));
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
                System.out.println("TEMP_DIRECTORY1::"+TEMP_DIRECTORY);
                fileInputa = createFinalDraftDoc(context, workflowProcessReferenceDocVersion, tempFile1html);

                paths = Files
                        .newDirectoryStream(Paths.get(TEMP_DIRECTORY), path -> path.toString().endsWith(".pdf")).iterator();

            }
        }
        // Read all pdf files and create base64
        while (paths.hasNext()) {
            Path path = paths.next();
            File pdf = path.toFile();
            InputStream pdfFile = new FileInputStream(pdf);
            byte[] pdfBytes = new byte[(int) pdf.length()];
            pdfFile.read(pdfBytes, 0, pdfBytes.length);
            pdfFile.close();
            String pdfStr = Base64.encodeBase64String(pdfBytes);
            //System.out.println("pdfstr::"+pdfStr);

            //single pdf sign
            System.out.println("Coordinates.BottomRight::::;;"+Coordinates.BottomMiddle);
          //  emBridgeSignerInput input = new emBridgeSignerInput(pdfStr, pdf.getName(), "Pune", "Distribution",context.getCurrentUser().getFullName(), true, PageTobeSigned.First, Coordinates.BottomRight, "", false);
            String Cordinate=PDFTextSearch.getCordinate(path.toAbsolutePath().toString(),"Signature_1");
            System.out.println("cordinate ::::"+Cordinate);
            String c="1-"+Cordinate;
            String location=(request.getRequstedData().getLocation()!=null?request.getRequstedData().getLocation():"-");
            String reason=(request.getRequstedData().getReason()!=null?request.getRequstedData().getReason():"-");
            emBridgeSignerInput emSignerInputforMultipleSignature4 = new emBridgeSignerInput(pdfStr, "4", "", "", "", true,
                    c, "Name : "+context.getCurrentUser().getFullName()+"\nLocation:"+location+".\nReason:"+reason+"", false);

            //cosign-multiple sign
            //emBridgeSignerInput emSignerInputforMultipleSignature4 = new emBridgeSignerInput(pdfStr, "4", "", "", "", true, "1-377,300,547,370;1-121,300,291,370", "Name:Solomon\nReason:Testing", false);

            // emBridgeSignerInput input = new emBridgeSignerInput(pdfStr,pdf.getName(),
            // "Bangalore", "Distribution", "eMudhra Limited",true, Coordinates.BottomRight,
            // "1,2,3");
            // emBridgeSignerInput input = new emBridgeSignerInput(pdfStr,pdf.getName(), "",
            // "","eMudhra Limited", true, "1-425,100,545,160;2-225,725,345,785","",false);
            inputs.add(emSignerInputforMultipleSignature4);
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
        // Generate encrrypted request for PKCSBulkSign API Call
        Request bulkPKCSSignRequest = bridge.encPKCSBulkSign(pKCSBulkPdfHashSignRequest);
        String r1 = bulkPKCSSignRequest.getEncryptedData();
        String r2 = bulkPKCSSignRequest.getEncryptionKeyID();
        req.setEncryptedRequest(r1);
        req.setEncryptionKeyId(r2);
        req.setTempfilepath(bulkPKCSSignRequest.getTempFilePath());
        req.setTempfolder(TEMP_DIRECTORY);
        return req;
    }

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = {RequestMethod.POST}, value = "/pdfSignRequestNote")
    public RequestData pdfSignRequestNote(@RequestBody EmudraRequestData request,HttpServletRequest requests) throws Exception {
        Context context = ContextUtil.obtainContext(requests);
        context.turnOffAuthorisationSystem();
        InputStream fileInputa = null;
        System.out.println("::::::pdfSign::::");
        RequestData rest = request.getRequstedData();
        final String TEMP_DIRECTORY=getFolderTmp("UnSign");
        RequestData req = new RequestData();
        String emBridgeLicense = configurationService.getProperty("em.bridge.license");
        String emBridgeLogs = configurationService.getProperty("em.bridge.logs");
        emBridge bridge = new emBridge(emBridgeLicense, emBridgeLogs);
        // Initiate emBridge Object
        List<emBridgeSignerInput> inputs = new ArrayList<>();
        // Get All pdf files from provided folder
        Iterator<Path> paths=null;
        String s = null;
        Integer index=0;
        System.out.println("doc id" + rest.getDocumentuuid());
        WorkflowProcessReferenceDoc workflowProcessReferenceDoc = workflowProcessReferenceDocService.find(context, UUID.fromString(rest.getDocumentuuid()));
        if (workflowProcessReferenceDoc != null) {

              WorkflowProcess workFlowProcess=workflowProcessReferenceDoc.getWorkflowProcess();
              if(workFlowProcess!=null){
                  Optional<Integer> countFalseIssignnote = workFlowProcess.getWorkflowProcessEpeople().stream()
                          .filter(d ->d.getePerson().getID().toString().equalsIgnoreCase(context.getCurrentUser().getID().toString())).map(d->d.getIndex()).findFirst();

                  if(countFalseIssignnote.isPresent()){

                    index=countFalseIssignnote.get();
                     index=index+1;
                      System.out.println(":::::::::::::index::::::::"+index);
                  }
              }
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
                System.out.println("TEMP_DIRECTORY1::"+TEMP_DIRECTORY);
               fileInputa = bitstreamService.retrieve(context, workflowProcessReferenceDoc.getBitstream());
                PdfUtils.writeInputStreamToPDF(fileInputa,tempFile1html.getAbsolutePath());
                paths = Files
                        .newDirectoryStream(Paths.get(TEMP_DIRECTORY), path -> path.toString().endsWith(".pdf")).iterator();

        }
        // Read all pdf files and create base64
        while (paths.hasNext()) {
            Path path = paths.next();
            File pdf = path.toFile();
            InputStream pdfFile = new FileInputStream(pdf);
            byte[] pdfBytes = new byte[(int) pdf.length()];
            pdfFile.read(pdfBytes, 0, pdfBytes.length);
            pdfFile.close();
            String pdfStr = Base64.encodeBase64String(pdfBytes);
            //System.out.println("pdfstr::"+pdfStr);

            //single pdf sign
  //          System.out.println("Coordinates.BottomRight::::;;"+Coordinates.BottomMiddle);
//            emBridgeSignerInput input=null;
//           if(index==1) {
//               input = new emBridgeSignerInput(pdfStr, pdf.getName(), "Pune", "Distribution", context.getCurrentUser().getFullName(), true, PageTobeSigned.First, Coordinates.TopRight, "", false);
//           }
//            if(index==2) {
//                input = new emBridgeSignerInput(pdfStr, pdf.getName(), "Pune", "Distribution", context.getCurrentUser().getFullName(), true, PageTobeSigned.First, Coordinates.BottomMiddle, "", false);
//            }
//            if(index==3) {
//               input = new emBridgeSignerInput(pdfStr, pdf.getName(), "Pune", "Distribution", context.getCurrentUser().getFullName(), true, PageTobeSigned.First, Coordinates.BottomRight, "", false);
//            }_Signature_"+i+"Name_:

            int pageno= PDFTextSearch.getPageNumberByText(index,path.toAbsolutePath().toString());
            String Cordinate=PDFTextSearch.getCordinate(path.toAbsolutePath().toString(),"Signature_"+index+"_Name:",pageno);
           int p=pageno+1;
            String c=p+"-"+Cordinate;
            System.out.println("cordinate ::::"+c);
            String location=(request.getRequstedData().getLocation()!=null?request.getRequstedData().getLocation():"-");
            String reason=(request.getRequstedData().getReason()!=null?request.getRequstedData().getReason():"-");

             String page=String.valueOf(pageno);
            emBridgeSignerInput emSignerInputforMultipleSignature4 = new emBridgeSignerInput(pdfStr, "4", "", "", "", true,
                    c, "Name:"+context.getCurrentUser().getFullName()+"\nLocation:"+location+"\nReason:"+reason+"", false);

            //cosign-multiple sign
            //emBridgeSignerInput emSignerInputforMultipleSignature4 = new emBridgeSignerInput(pdfStr, "4", "", "", "", true, "1-377,300,547,370;1-121,300,291,370", "Name:Solomon\nReason:Testing", false);

            // emBridgeSignerInput input = new emBridgeSignerInput(pdfStr,pdf.getName(),
            // "Bangalore", "Distribution", "eMudhra Limited",true, Coordinates.BottomRight,
            // "1,2,3");
            // emBridgeSignerInput input = new emBridgeSignerInput(pdfStr,pdf.getName(), "",
            // "","eMudhra Limited", true, "1-425,100,545,160;2-225,725,345,785","",false);
            inputs.add(emSignerInputforMultipleSignature4);
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
        // Generate encrrypted request for PKCSBulkSign API Call
        Request bulkPKCSSignRequest = bridge.encPKCSBulkSign(pKCSBulkPdfHashSignRequest);
        String r1 = bulkPKCSSignRequest.getEncryptedData();
        String r2 = bulkPKCSSignRequest.getEncryptionKeyID();
        req.setEncryptedRequest(r1);
        req.setEncryptionKeyId(r2);
        req.setTempfilepath(bulkPKCSSignRequest.getTempFilePath());
        req.setTempfolder(TEMP_DIRECTORY);
        this.isdraftnotesin=true;
        return req;
    }

    public void signdone(Context context,WorkflowProcessReferenceDoc workflowProcessReferenceDoc){
        try {
            System.out.println("::::::::::::::::done::::::::::::::::");
            WorkflowProcess workFlowProcess = workflowProcessReferenceDoc.getWorkflowProcess();
            if(workFlowProcess!=null) {
                long countFalseIssignnote = workFlowProcess.getWorkflowProcessEpeople().stream()
                        .filter(d -> !d.getIssignnote())
                        .count();

                System.out.println("count::::::::::::::::"+countFalseIssignnote);
                if (countFalseIssignnote == 1) {
                    workFlowProcess.setIssignnote(true);
                    workflowProcessService.update(context, workFlowProcess);
                    System.out.println("final done ::::::::::::::::::::::");
                }
                Optional<WorkflowProcessEperson> current = workFlowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getePerson().getID().toString().equalsIgnoreCase(context.getCurrentUser().getID().toString())).findFirst();
                if (current.isPresent()) {
                    WorkflowProcessEperson e = current.get();
                    e.setIssignnote(true);
                    workflowProcessEpersonService.update(context, e);
                    System.out.println("::::::::current::WorkflowProcessEperson::setIssignnote::::done::::::::::::::::");
                }

            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);

        } catch (AuthorizeException e) {
            throw new RuntimeException(e);
        }
    }

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = {RequestMethod.POST}, value = "/pdfSignResponse")
    public RequestData pdfSignResponse(@RequestBody RequestData request,HttpServletRequest requests) throws Exception {
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
           return req;
       }catch (Exception e){
           e.printStackTrace();
           return null;
       }

    }

    public static String getFolderTmp(String foldername){
        final String TEMP_DIRECTORY1 = System.getProperty("java.io.tmpdir");
        Date now = new Date(System.currentTimeMillis() + 3 * 60 * 1000);
        String sdf = new SimpleDateFormat("ddMMMyyyyHHmmss").format(now);
        File theDir = new File(TEMP_DIRECTORY1+"\\"+foldername+"_"+sdf);
        if (!theDir.exists()){
            theDir.mkdirs();
        }
        return theDir.getAbsoluteFile().getAbsolutePath();
    }
    public  Bitstream generatePDFFile(Context context,ResponseDataPKCSBulkSign serviceResponse, RequestData request) throws FileNotFoundException, IOException, SQLException, AuthorizeException {
       Bitstream bitstreamsing=null;
        String filePath = request.getTempfilepath();
        String folderpath= request.getTempfolder();
        System.out.println("folder "+filePath);
        System.out.println("filePath "+folderpath);
        File tempFile = new File(filePath);

        SimpleDateFormat tsFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date now = new Date(System.currentTimeMillis() + 3 * 60 * 1000);
        String timeStamp = tsFormat.format(now);
        System.out.println("time Stamp" + timeStamp);
        String sdf = new SimpleDateFormat("ddMMMyyyyHHmmss").format(now);
        final String TEMP_DIRECTORY1 =getFolderTmp("Sign");
        int count = 1;
        for (BulkSignOutput doc : serviceResponse.getBulkSignItems()) {
            Random random = new Random();
            // Generate a random 4-digit number
            int randomNumber = random.nextInt(9000) + 1000;
            byte[] signedDocBytes = Base64.decodeBase64(doc.getSignedData());
            File file = new File(TEMP_DIRECTORY1,"sing_"+sdf+randomNumber+".pdf");
            System.out.println("sing doc"+ file.getAbsolutePath());
            OutputStream os = new FileOutputStream(file);
            os.write(signedDocBytes);
            os.close();
            count++;
            FileInputStream outputfile = new FileInputStream(new File(file.getAbsolutePath()));
            System.out.println(" sing doc file.getAbsolutePath() "+ file.getAbsolutePath());
            Bitstream bitstream = bundleRestRepository.processBitstreamCreationWithoutBundle(context, outputfile, "", file.getName());
            bitstreamsing=bitstream;
            WorkflowProcessReferenceDoc workflowProcessReferenceDoc = workflowProcessReferenceDocService.find(context, UUID.fromString(request.getDocumentuuid()));
            if (workflowProcessReferenceDoc != null) {
                Optional<WorkflowProcessReferenceDocVersion> workflowProcessReferenceDocVersion = workflowProcessReferenceDoc.getWorkflowProcessReferenceDocVersion().stream().filter(d->d!=null).filter(i -> i.getIsactive()).findFirst();
               if(workflowProcessReferenceDocVersion.isPresent()) {
                   WorkflowProcessReferenceDocVersion v = workflowProcessReferenceDocVersionService.find(context, workflowProcessReferenceDocVersion.get().getID());
                   if (v != null) {
                       if (bitstream != null) {
                           v.setBitstream(bitstream);
                       }
                       workflowProcessReferenceDocVersionService.update(context, v);
                       if (bitstream != null) {
                           workflowProcessReferenceDoc.setBitstream(bitstream);
                       }
                       System.out.println("bitstreampdfsing::" + bitstream.getID());
                   }
               }
                System.out.println("bitstreampdfsing::" + bitstream.getID());
               if(isdraftnotesin){
                   signdone(context,workflowProcessReferenceDoc);
               }
                workflowProcessReferenceDoc.setBitstream(bitstreamsing);
                workflowProcessReferenceDocService.update(context, workflowProcessReferenceDoc);
            }
            context.commit();
          //  file.delete();
        }
        tempFile.delete();
        System.out.println(""+deleteFolderTmp(folderpath));
       return bitstreamsing;

    }





    public static String deleteFolderTmp(String s){
        File theDir = new File(s);
        if (theDir.exists()) {
            theDir.delete();
            return "deleted "+theDir.getAbsolutePath();
        }
        return "already deleted";
    }


//this code text sign only
    //    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
//    @RequestMapping(method = {RequestMethod.POST}, value = "/PKCSSignRequest")
//    public RequestData PKCSSignRequest(@RequestBody EmudraRequestData request) throws Exception {
//        System.out.println("::::::::::");
//        RequestData req = new RequestData();
//        String emBridgeLicense = configurationService.getProperty("em.bridge.license");
//        String emBridgeLogs = configurationService.getProperty("em.bridge.logs");
//        emBridge bridge = new emBridge(emBridgeLicense, emBridgeLogs);
//        PKCSSignRequest pKCSSignRequest=null;
//        if(request.getRequstedData()!=null) {
//            pKCSSignRequest = new PKCSSignRequest();
//            pKCSSignRequest.setKeyStoreDisplayName(request.getRequstedData().getKeyStoreDisplayName());
//            pKCSSignRequest.setKeyStorePassphrase(request.getRequstedData().getKeyStorePassphrase());
//            pKCSSignRequest.setKeyId(request.getRequstedData().getKeyId());
//            pKCSSignRequest.setDataType(emh.Enum.ContentType.TextPKCS7); // detached data signature
//            //pKCSSignRequest.setDataType(ContentType.TextPKCS7ATTACHED); //attached data
//            // signature
//            pKCSSignRequest.setDataToSign(request.getRequstedData().getDataToSign());
//            pKCSSignRequest.setTimeStamp(""+ LocalTime.now());
//        }
//
//        Request data = bridge.encPKCSSign(pKCSSignRequest);
//        System.out.println("Encrypted Data :" + data.getEncryptedData());
//        System.out.println("Encrypted Key ID :" + data.getEncryptionKeyID());
//        String r1 = data.getEncryptedData();
//        String r2 = data.getEncryptionKeyID();
//        req.setEncryptedRequest(r1);
//        req.setEncryptionKeyId(r2);
//        return req;
//    }
//
//    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
//    @RequestMapping(method = {RequestMethod.POST}, value = "/GetPKCSSignData")
//    public ResponseDataPKCSSign GetPKCSSignData(@RequestBody RequestData data) throws Exception {
//        try {
//            String emBridgeLicense = configurationService.getProperty("em.bridge.license");
//            String emBridgeLogs = configurationService.getProperty("em.bridge.logs");
//            emBridge bridge = new emBridge(emBridgeLicense, emBridgeLogs);
//            ResponseDataPKCSSign responseDataPKCSSign;
//            responseDataPKCSSign = bridge.decPKCSSign(data.getEncryptedRequest());
//            return responseDataPKCSSign;
//        }catch (Exception e){
//            e.printStackTrace();
//            return null;
//        }
//    }




// emudra APIS code end


    public FileInputStream createFinalDraftDoc(Context context, WorkflowProcessReferenceDocVersion workflowProcessReferenceDoc, File tempFile1html) throws Exception {
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
        sb.append("<br><div style=\"float:right;padding-right:150px;\">");
        sb.append("<p>"+DateUtils.DateToSTRDDMMYYYHHMMSS(new Date())+"</p> </div> <br>");
        if (workflowProcessReferenceDoc.getEditortext() != null) {
            isTextEditorFlow = true;
            sb.append("<div>" + workflowProcessReferenceDoc.getEditortext() + "</div>");
        }
        sb.append("<br><br><br><div style=\" float: right; width:20%;\"><p> <B>Signature_1</B> </p><B><span></div>");
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


}
