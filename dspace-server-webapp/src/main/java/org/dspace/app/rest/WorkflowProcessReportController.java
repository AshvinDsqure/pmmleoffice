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
import org.dspace.app.rest.converter.WorkflowProcessReferenceDocConverter;
import org.dspace.app.rest.model.*;
import org.dspace.app.rest.repository.BundleRestRepository;
import org.dspace.app.rest.utils.*;
import org.dspace.content.Item;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.ParseException;
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
        + "/workflowreport")
public class WorkflowProcessReportController {
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


    @RequestMapping(method = RequestMethod.GET, value = "/withinDepartmentDownload")
    public ResponseEntity<Resource> withinDepartmentReport(HttpServletRequest request,
                                                           @Parameter(value = "startdate", required = true) String startdate,
                                                           @Parameter(value = "enddate", required = true) String enddate) {
        try {
            Context context = ContextUtil.obtainContext(request);
            HashMap<String, String> map = new HashMap<>();
            System.out.println(map);
            String startdatestr = startdate.replace("-", "_");
            String enddatestr = enddate.replace("-", "_");

            String filename = "Days_Within_Department_" + startdatestr + "_To_" + enddatestr + "_Report.xlsx";
            List<Object[]> list = workflowProcessService.withinDepartmentDownload(context, map, startdate, enddate);
            System.out.println("List----" + list.size());

            if (list != null && list.size() != 0) {
                List<WithinDepartmentDTO> rests = list.stream().filter(d -> d[3] != null && d[3].toString().equalsIgnoreCase("Inner")).map(d -> {
                    WithinDepartmentDTO dd = new WithinDepartmentDTO();
                    dd.setName(d[0].toString());
                    dd.setDays(Long.parseLong(d[1].toString()));
                    dd.setFilecount(Long.parseLong(d[2].toString()));
                    return dd;
                }).collect(Collectors.toList());
                List<String> uniqueNames = rests.stream()
                        .map(WithinDepartmentDTO::getName)
                        .distinct()
                        .collect(Collectors.toList());
                ByteArrayInputStream in = ExcelHelper.getwithinDepartmentReport(rests, uniqueNames);
                InputStreamResource file = new InputStreamResource(in);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                        .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                        .body(file);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("error :" + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);

        } catch (Exception e) {
            System.out.println("error :" + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/noOfFileCreatedAndClose")
    public ResponseEntity<Resource> noOfFileCreatedAndClose(HttpServletRequest request,
                                                            @Parameter(value = "startdate", required = true) String startdate,
                                                            @Parameter(value = "enddate", required = true) String enddate) {
        try {
            Context context = ContextUtil.obtainContext(request);
            HashMap<String, String> map = new HashMap<>();
            System.out.println(map);
            String startdatestr = startdate.replace("-", "_");
            String enddatestr = enddate.replace("-", "_");
            String filename = "NoOfFileCreatedAndClosed_" + startdatestr + "_To_" + enddatestr + "_Report.xlsx";
            System.out.println("file name::" + filename);
            List<Object[]> list = workflowProcessService.NoOfFileCreatedAndClose(context, map, startdate, enddate);
            System.out.println("----List----" + list.size());
            if (list != null && list.size() != 0) {
                List<WithinDepartmentDTO> rests = list.stream().filter(d -> d[0] != null && d[1] != null && d[2] != null).map(d -> {
                    WithinDepartmentDTO dd = new WithinDepartmentDTO();
                    dd.setName(d[0].toString());
                    dd.setDays(Long.parseLong(d[1].toString()));
                    dd.setFilecount(Long.parseLong(d[2].toString()));
                    return dd;
                }).collect(Collectors.toList());
                ByteArrayInputStream in = ExcelHelper.getNoOFFileCreatedAndClosedToExcel(rests);
                InputStreamResource file = new InputStreamResource(in);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                        .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                        .body(file);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("error :" + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);

        } catch (Exception e) {
            System.out.println("error :" + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/noOfTapalCreatedAndClose")
    public ResponseEntity<Resource> noOfTapalCreatedAndClose(HttpServletRequest request,
                                                             @Parameter(value = "startdate", required = true) String startdate,
                                                             @Parameter(value = "enddate", required = true) String enddate) {
        try {
            Context context = ContextUtil.obtainContext(request);
            HashMap<String, String> map = new HashMap<>();
            System.out.println(map);
            String startdatestr = startdate.replace("-", "_");
            String enddatestr = enddate.replace("-", "_");
            String filename = "noOfTapalCreatedAndClose_" + startdatestr + "_To_" + enddatestr + "_Report.xlsx";
            System.out.println("file name::" + filename);
            List<Object[]> list = workflowProcessService.NoOfTapalCreatedAndClose(context, map, startdate, enddate);
            System.out.println("----List----" + list.size());
            if (list != null && list.size() != 0) {
                List<WithinDepartmentDTO> rests = list.stream().filter(d -> d[0] != null && d[1] != null && d[2] != null).map(d -> {
                    WithinDepartmentDTO dd = new WithinDepartmentDTO();
                    dd.setName(d[0].toString());
                    dd.setDays(Long.parseLong(d[1].toString()));
                    dd.setFilecount(Long.parseLong(d[2].toString()));
                    return dd;
                }).collect(Collectors.toList());
                ByteArrayInputStream in = ExcelHelper.getNoOFTapalCreatedAndClosedToExcel(rests);
                InputStreamResource file = new InputStreamResource(in);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                        .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                        .body(file);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("error :" + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);

        } catch (Exception e) {
            System.out.println("error :" + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/outerDepartmentDownload")
    public ResponseEntity<Resource> outerDepartmentDownload(HttpServletRequest request,
                                                            @Parameter(value = "startdate", required = true) String startdate,
                                                            @Parameter(value = "enddate", required = true) String enddate) {
        try {
            Context context = ContextUtil.obtainContext(request);
            HashMap<String, String> map = new HashMap<>();
            String startdatestr = startdate.replace("-", "_");
            String enddatestr = enddate.replace("-", "_");
            String filename = "Inter_Department_" + startdatestr + "_To_" + enddatestr + "_Report.xlsx";

            List<Object[]> list = workflowProcessService.withinDepartmentDownload(context, map, startdate, enddate);
            System.out.println("List size::" + list.size());

            if (list != null && list.size() != 0) {
                List<WithinDepartmentDTO> rests = list.stream().filter(d -> d[0] != null && d[1] != null && d[3] != null && d[3].toString().equalsIgnoreCase("Outer")).map(d -> {
                    WithinDepartmentDTO dd = new WithinDepartmentDTO();
                    dd.setName(d[0].toString());
                    dd.setDays(Long.parseLong(d[1].toString()));
                    dd.setFilecount(Long.parseLong(d[2].toString()));
                    return dd;
                }).collect(Collectors.toList());
                List<String> uniqueNames = rests.stream()
                        .map(WithinDepartmentDTO::getName)
                        .distinct()
                        .collect(Collectors.toList());
                ByteArrayInputStream in = ExcelHelper.getouterDepartmentReport(rests, uniqueNames);
                InputStreamResource file = new InputStreamResource(in);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                        .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                        .body(file);
            }
        } catch (SQLException e) {
            System.out.println("error :" + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);

        }
        return null;
    }


    @RequestMapping(method = RequestMethod.GET, value = "/stagewithinDepartmentReport")
    public ResponseEntity<Resource> stagewithinDepartmentReport(HttpServletRequest request,
                                                                @Parameter(value = "startdate", required = true) String startdate,
                                                                @Parameter(value = "enddate", required = true) String enddate) {
        try {
            Context context = ContextUtil.obtainContext(request);
            HashMap<String, String> map = new HashMap<>();
            String name = "_";
            String startdatestr = startdate.replace("-", "_");
            String enddatestr = enddate.replace("-", "_");
            System.out.println(map);
            String filename = "Stage_Within_Department_" + startdatestr + "_To_" + enddatestr + "_Report.xlsx";
            List<Object[]> list = workflowProcessService.stagewithinDepartmentDownload(context, map, startdate, enddate);
            System.out.println("List----" + list.size());

            if (list != null && list.size() != 0) {
                List<WithinDepartmentDTO> rests = list.stream().filter(d -> d[0] != null && d[1] != null && d[3] != null && d[3].toString().equalsIgnoreCase("Inner")).map(d -> {
                    WithinDepartmentDTO dd = new WithinDepartmentDTO();
                    dd.setName(d[0].toString());
                    dd.setDays(Long.parseLong(d[1].toString()));
                    dd.setFilecount(Long.parseLong(d[2].toString()));
                    return dd;
                }).collect(Collectors.toList());
                List<String> uniqueNames = rests.stream()
                        .map(WithinDepartmentDTO::getName)
                        .distinct()
                        .collect(Collectors.toList());
                ByteArrayInputStream in = ExcelHelper.getstagewithinDepartmentReport(rests, uniqueNames);
                InputStreamResource file = new InputStreamResource(in);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                        .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                        .body(file);
            }
        } catch (SQLException e) {
            System.out.println("error :" + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);

        }
        return null;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/stageouterDepartmentDownload")
    public ResponseEntity<Resource> stageouterDepartmentDownload(HttpServletRequest request,
                                                                 @Parameter(value = "startdate", required = true) String startdate,
                                                                 @Parameter(value = "enddate", required = true) String enddate) {
        try {
            Context context = ContextUtil.obtainContext(request);
            HashMap<String, String> map = new HashMap<>();
            System.out.println(map);
            String startdatestr = startdate.replace("-", "_");
            String enddatestr = enddate.replace("-", "_");
            System.out.println(map);
            String filename = "Stage_Inter_Department_" + startdatestr + "_To_" + enddatestr + "_Report.xlsx";
            List<Object[]> list = workflowProcessService.stageouterDepartmentDownload(context, map, startdate, enddate);
            System.out.println("List----" + list.size());

            if (list != null && list.size() != 0) {
                List<WithinDepartmentDTO> rests = list.stream().filter(d -> d[0] != null && d[1] != null && d[3] != null && d[3].toString().equalsIgnoreCase("Outer")).map(d -> {
                    WithinDepartmentDTO dd = new WithinDepartmentDTO();
                    dd.setName(d[0].toString());
                    dd.setDays(Long.parseLong(d[1].toString()));
                    dd.setFilecount(Long.parseLong(d[2].toString()));
                    return dd;
                }).collect(Collectors.toList());
                List<String> uniqueNames = rests.stream()
                        .map(WithinDepartmentDTO::getName)
                        .distinct()
                        .collect(Collectors.toList());
                ByteArrayInputStream in = ExcelHelper.getstageouterDepartmentReport(rests, uniqueNames);
                InputStreamResource file = new InputStreamResource(in);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                        .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                        .body(file);
            }
        } catch (SQLException e) {
            System.out.println("error :" + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);

        }
        return null;
    }


    @RequestMapping(method = RequestMethod.GET, value = "/withinDepartmentDownloadAPI")
    public Map<String, List<WithinDepartmentDTO>> withinDepartmentDownloadAPI(HttpServletRequest request,
                                                                              @Parameter(value = "startdate", required = true) String startdate,
                                                                              @Parameter(value = "enddate", required = true) String enddate) {
        try {
            Context context = ContextUtil.obtainContext(request);
            HashMap<String, String> map = new HashMap<>();
            String startdatestr = startdate.replace("-", "_");
            String enddatestr = enddate.replace("-", "_");
            System.out.println(map);
            List<Object[]> list = workflowProcessService.withinDepartmentDownload(context, map, startdate, enddate);
            System.out.println("List----" + list.size());
            Map<String, List<WithinDepartmentDTO>> mapa = new LinkedHashMap<>();
            if (list != null && list.size() != 0) {
                List<WithinDepartmentDTO> rests = list.stream().filter(d -> d[0] != null && d[1] != null && d[2] != null&&d[3]!=null).map(d -> {
                    WithinDepartmentDTO dd = new WithinDepartmentDTO();
                    dd.setName(d[0].toString());
                    dd.setDays(Long.parseLong(d[1].toString()));
                    dd.setFilecount(Long.parseLong(d[2].toString()));
                    dd.setType(d[3].toString());
                    return dd;
                }).collect(Collectors.toList());
                mapa.put("data", rests);
                List<WithinDepartmentDTO> uniqueNames = rests.stream()
                        .filter(dto -> dto.getName() != null && !dto.getName().isEmpty())
                        .collect(Collectors.toMap(
                                WithinDepartmentDTO::getName, // key = name
                                dto -> dto,                   // value = original DTO
                                (existing, replacement) -> existing)) // handle duplicate keys
                        .values()
                        .stream()
                        .collect(Collectors.toList());
                mapa.put("uniqueNames", uniqueNames);
                return mapa;
            }
        } catch (SQLException e) {
            System.out.println("error :" + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    @RequestMapping(method = RequestMethod.GET, value = "/stagewithinDepartmentReportAPI")
    public Map<String, List<WithinDepartmentDTO>> stagewithinDepartmentReportAPI(HttpServletRequest request,
                                                                                 @Parameter(value = "startdate", required = true) String startdate,
                                                                                 @Parameter(value = "enddate", required = true) String enddate) {
        try {
            Context context = ContextUtil.obtainContext(request);
            HashMap<String, String> map = new HashMap<>();
            List<Object[]> list = workflowProcessService.stagewithinDepartmentDownload(context, map, startdate, enddate);
            System.out.println("List----" + list.size());
            Map<String, List<WithinDepartmentDTO>> mapa = new LinkedHashMap<>();
            if (list != null && list.size() != 0) {
                List<WithinDepartmentDTO> rests = list.stream().filter(d -> d[0] != null && d[1] != null && d[3] != null).map(d -> {
                    WithinDepartmentDTO dd = new WithinDepartmentDTO();
                    dd.setName(d[0].toString());
                    dd.setDays(Long.parseLong(d[1].toString()));
                    dd.setFilecount(Long.parseLong(d[2].toString()));
                    dd.setType(d[3].toString());
                    return dd;
                }).collect(Collectors.toList());
                mapa.put("data", rests);
                List<WithinDepartmentDTO> uniqueNames = rests.stream()
                        .filter(dto -> dto.getName() != null && !dto.getName().isEmpty())
                        .collect(Collectors.toMap(
                                WithinDepartmentDTO::getName, // key = name
                                dto -> dto,                   // value = original DTO
                                (existing, replacement) -> existing)) // handle duplicate keys
                        .values()
                        .stream()
                        .collect(Collectors.toList());
                mapa.put("uniqueNames", uniqueNames);
                return mapa;
            }
        } catch (SQLException e) {
            System.out.println("error :" + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);

        }
        return null;
    }


    @RequestMapping(method = RequestMethod.GET, value = "/withinDepartmentDownloadAPICal")
    public FormulaDTO withinDepartmentDownloadAPICAL(HttpServletRequest request,
                                                     @Parameter(value = "startdate", required = true) String startdate,
                                                     @Parameter(value = "enddate", required = true) String enddate) {
        try {
            Context context = ContextUtil.obtainContext(request);
            HashMap<String, String> map = new HashMap<>();

            System.out.println(map);
            FormulaDTO formulaDTO = new FormulaDTO();
            List<Long> dayWiseDuration = new ArrayList<>();
            List<Long> stagewiseDuration = new ArrayList<>();
            long totalcreateFile = 0;
            long totalcloseFile = 0;
            long totalcreateTaPAL = 0;
            long totalcloseTaPAL = 0;

            List<Object[]> FILEs = workflowProcessService.NoOfFileCreatedAndClose(context, map, startdate, enddate);
            //file
            if (FILEs != null && FILEs.size() != 0) {
                totalcreateFile = FILEs.stream()
                        .filter(d -> d[1] != null)
                        .mapToLong(d -> Long.parseLong(d[1].toString()))
                        .sum();

                totalcloseFile = FILEs.stream()
                        .filter(d -> d[2] != null)
                        .mapToLong(d -> {
                            long value = Long.parseLong(d[2].toString());
                            return value;
                        })
                        .sum();

            }
            //tapal
            List<Object[]> TaPAL = workflowProcessService.NoOfTapalCreatedAndClose(context, map, startdate, enddate);
            if (TaPAL != null && TaPAL.size() != 0) {
                totalcreateTaPAL = TaPAL.stream()
                        .filter(d -> d[1] != null)
                        .mapToLong(d -> Long.parseLong(d[1].toString()))
                        .sum();
                totalcloseTaPAL = TaPAL.stream()
                        .filter(d -> d[2] != null)
                        .mapToLong(d -> Long.parseLong(d[2].toString()))
                        .sum();


            }
            formulaDTO.setTotalFilecreate(totalcreateFile);
            formulaDTO.setTotalFileclose(totalcloseFile);
            formulaDTO.setTotaltapalcreate(totalcreateTaPAL);
            formulaDTO.setTotaltapaleclose(totalcloseTaPAL);
            double rate = ((double) totalcloseFile / totalcreateFile) * 100;
            formulaDTO.setFileDisposalRate(rate);

            //days
            List<Object[]> Days = workflowProcessService.withinDepartmentDownload(context, map, startdate, enddate);
            if (Days != null && Days.size() != 0) {
                long group1 = 0; // < 8 days
                long group2 = 0; // 8 to <15 days
                long group3 = 0; // 15 to <22 days
                long group4 = 0; // 22 to ≤30 days
                for (Object[] d : Days) {
                    if (d[1] != null && d[2] != null) {
                        long daysTaken = Long.parseLong(d[1].toString());
                        long fileCounts = Long.parseLong(d[2].toString());
                        if (daysTaken < 8) {
                            group1 += fileCounts;
                        } else if (daysTaken < 15) {
                            group2 += fileCounts;
                        } else if (daysTaken < 22) {
                            group3 += fileCounts;
                        } else if (daysTaken < 100000) {
                            group4 += fileCounts;
                        }
                    }
                }
                long tot = group1 + group2 + group3 + group4;
                if (tot < totalcloseFile) {
                    long s = totalcloseFile - tot;
                    group4 += s;
                }
                dayWiseDuration.add(group1);
                dayWiseDuration.add(group2);
                dayWiseDuration.add(group3);
                dayWiseDuration.add(group4);
                formulaDTO.setDayWiseDuration(dayWiseDuration);
            }
            List<Object[]> stages = workflowProcessService.stagewithinDepartmentDownload(context, map, startdate, enddate);
            if (stages != null && stages.size() != 0) {
                long stagegroup1 = 0; // < 8 days
                long stagegroup2 = 0; // 8 to < 15 days
                long stagegroup3 = 0; // 15 to < 22 days
                long stagegroup4 = 0; // 22 to ≤ 30 days
                for (Object[] d : stages) {
                    if (d[1] != null && d[2] != null) {
                        long stageTaken = Long.parseLong(d[1].toString());
                        long fileCount = Long.parseLong(d[2].toString());
                        if (stageTaken < 8) {
                            stagegroup1 += fileCount;
                        } else if (stageTaken < 15) {
                            stagegroup2 += fileCount;
                        } else if (stageTaken < 22) {
                            stagegroup3 += fileCount;
                        } else if (stageTaken < 100000) {
                            stagegroup4 += fileCount;
                        }
                    }
                }
                stagewiseDuration.add(stagegroup1);
                stagewiseDuration.add(stagegroup2);
                stagewiseDuration.add(stagegroup3);
                stagewiseDuration.add(stagegroup4);
                formulaDTO.setStagewiseDuration(stagewiseDuration);
                formulaDTO.setTotleuser(ePersonService.countTotal(context));
                formulaDTO.setActiveuser(ePersonService.activeUsercount(context));
            }
            return formulaDTO;
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
