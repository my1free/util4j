package com.michealyang.util4j.excel;

import com.google.common.collect.Maps;
import com.michealyang.util4j.excel.convert.RowConvert;
import com.michealyang.util4j.excel.convert.WorkBookConvert;
import com.michealyang.util4j.excel.util.DateTimeUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author michealyang
 * @version 1.0
 * @created 18/6/11
 * 开始眼保健操： →_→  ↑_↑  ←_←  ↓_↓
 */

public class ExcelExporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelExporter.class);


    public static final String EXCEL_EXT_2003 = "xls";

    public static final String EXCEL_EXT_2007 = "xlsx";


    public static final String HEADER_CONTENT_DISPOSITON = "Content-Disposition";

    public static final String HEADER_CONTENT_TYPE = "Content-Type";


    /**
     * 解析excel文件流到list中
     *
     * @param input     excel文件流
     * @param sheetNum  sheet页
     * @param fileName  文件名,主要用来判断是
     * @param clazz     转化数据到目标类
     * @param <T>       泛型目标类
     * @param hasHeader excel是否有表头,默认第一行是有表头的;true有表头,false没有表头
     * @return 数据集合list
     */
    public static <T> List<T> parseData(InputStream input, Integer sheetNum, String fileName, Class<T> clazz, boolean hasHeader) {
        Iterator<Row> rows = getExcelRows(input, sheetNum, isE2007(fileName));
        return RowConvert.convertWithConstructor(rows, clazz, hasHeader);
    }


    /**
     * 反序列化多Sheet文件流
     * @param inputStream
     * @param clazz
     * @param fileName
     * @param hasHeader
     * @param <T>
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    public static <T> T parseDate(InputStream inputStream, Class<T> clazz, String fileName, boolean hasHeader) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Map<String,Sheet> sheetMap = getExcelSheets(inputStream,isE2007(fileName));
        return RowConvert.convertWithConstructor(sheetMap,clazz,hasHeader);
    }


    /**
     * 根据excel文件绝对地址解析excel
     *
     * @param filePathName excel文件绝对地址
     * @param sheetNum     sheet页
     * @param <T>          泛型目标类
     * @param hasHeader    excel是否有表头,默认第一行是有表头的;true有表头,false没有表头
     * @return 数据集合list
     */
    public static <T> List<T> parseData(String filePathName, Integer sheetNum, Class<T> clazz, boolean hasHeader) throws FileNotFoundException {
        InputStream input = new FileInputStream(filePathName);  //建立输入流
        return parseData(input, sheetNum, getFileName(filePathName), clazz, hasHeader);
    }


    /**
     * 根据数据集合导出到excel中
     * 以ExcelCellField中的name作为表头,index映射到excel列上
     *
     * @param dataList     数据集合
     * @param filePathName 文件名称(全路径)
     * @param <T>          集合数据的泛型
     */
    public static <T> void export(List<T> dataList, String filePathName) throws IOException {
        File outfile = new File(filePathName);
        if (!outfile.exists()) {
            outfile.createNewFile();
        }
        FileOutputStream outputStream = new FileOutputStream(outfile);
        export(dataList, outputStream);
        outputStream.flush();
        outputStream.close();
    }


    /**
     * 导出Excel
     * @param data
     * @param <T>
     * @return
     */
    public static <T> byte[] export(T data){
        Workbook workbook = WorkBookConvert.convert(data);
        ByteArrayOutputStream os = null;
        try {
            os = new ByteArrayOutputStream();
            workbook.write(os);
            byte[] bytes = os.toByteArray();
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (os != null) {
                try {
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    //Do Nothing;
                }
            }
        }
    }




    /**
     * 根据list导出 OutputStream 到HttpServletResponse
     * 以ExcelCellField中的name作为表头,index映射到excel列上
     * 支持多浏览器文件名编码正确
     * <p>
     * <p>
     * 使用场景: controller下载文件.根据sql找到数据集合,转换为HttpServletResponse.OutputStream进行文件下载
     *
     * @param dataList 数据集合
     * @param fileName 文件名称(下载下来的文件名称,默认名称为[导出文件yyyy-MM-dd hh:mm] 名称中的时间为当前时间)
     * @param response controller得到的用户请求返回
     * @param <T>      数据类泛型
     * @throws IOException    Workbook.write(OutputStream) 会扔出异常
     * @throws ParseException 默认名称用
     */
    public static <T> void export(List<T> dataList, String fileName, HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException {
        setResponse(request, response, fileName);
        export(dataList, response.getOutputStream());
    }

    /**
     * 根据list导出到OutputStream
     * 以ExcelCellField中的name作为表头,index映射到excel列上
     * <p>
     * 可以使excel文件输出到不同类型的OutputStream
     *
     * @param dataList 数据集合
     * @param output   文件输出流
     * @param <T>      泛型类
     * @throws IOException
     */
    public static <T> void export(List<T> dataList, OutputStream output) throws IOException {
        if (CollectionUtils.isEmpty(dataList)) {
            return;
        }
        Workbook wb = WorkBookConvert.convert(dataList);
        wb.write(output);
    }

    public static <T> void exportInStream(List<T> dataList, OutputStream outputStream) throws IOException {
        if (CollectionUtils.isEmpty(dataList)) {
            return;
        }
        Workbook wb = WorkBookConvert.convertInStream(dataList);
        wb.write(outputStream);
    }

    /**
     * 给response设置可以Header,可以直接文件下载
     * 支持多浏览器的下载
     * <p>
     *
     * @param response 返回请求
     * @param fileName 下载的文件名
     * @throws UnsupportedEncodingException
     * @throws ParseException
     */
    public static void setResponse(HttpServletRequest request, HttpServletResponse response, String fileName) throws UnsupportedEncodingException, ParseException {
        //默认名称
        if (fileName == null) {
            fileName = "导出文件" + DateTimeUtils.convertDateToString(new Date(), DateTimeUtils.DATE_TIME_FORMAT);
        }
        fileName = getEncodeFileName(fileName, request);
        if (!fileName.endsWith(EXCEL_EXT_2003) && !fileName.endsWith(EXCEL_EXT_2007)) {
            fileName = fileName + "." + EXCEL_EXT_2007;
        }
        setResponseHeader(response, fileName);
    }

    /**
     * 根据浏览器设置文件编码格式
     * 支持多浏览器的文件名格式
     * <p>
     * 备注个浏览器User-Agent
     * chrome : Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.154 Safari/537.36
     * FIREFOX:Mozilla/5.0 (Windows NT 6.1; WOW64; rv:32.0) Gecko/20100101 Firefox/32.0
     * IE8 : Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Win64; x64; Trident/4.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; InfoPath.3; .NET4.0C; .NET4.0E)
     * IE9 : Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)
     * IE10 : Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:10.0) like Gecko
     * IE11 : Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko
     * 360 极速模式:Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.122 Safari/537.36
     * 360 的IE9模式:Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)
     * Edge:
     *
     * @param fileName 文件名
     * @param request  请求
     */
    public static String getEncodeFileName(String fileName, HttpServletRequest request) throws UnsupportedEncodingException {
        final String userAgent = request.getHeader("USER-AGENT");
        LOGGER.debug("userAgent={}", userAgent);
        String finalFileName = null;
        if (StringUtils.contains(userAgent, "MSIE") || StringUtils.contains(userAgent, "Trident") || StringUtils.contains(userAgent, "Edge")) {//IE浏览器
            finalFileName = URLEncoder.encode(fileName, "UTF8");
            LOGGER.debug("IE,finalFileName={}", finalFileName);
        } else if (StringUtils.contains(userAgent, "Mozilla")) {//google,火狐浏览器
            finalFileName = new String(fileName.getBytes(), "ISO8859-1");
            LOGGER.debug("google,火狐浏览器 ,finalFileName={}", finalFileName);
        } else {
            finalFileName = URLEncoder.encode(fileName, "UTF8");//其他浏览器
            LOGGER.debug("others,finalFileName={}", finalFileName);
        }
        return finalFileName;
    }

    private static void setResponseHeader(HttpServletResponse response, String fileName) {
        if (StringUtils.isEmpty(response.getHeader(HEADER_CONTENT_DISPOSITON))) {
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        }
        if (StringUtils.isEmpty(response.getHeader(HEADER_CONTENT_TYPE))) {
            response.setHeader("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }
    }

    public static String getFileName(String filePathName) {
        File tempFile = new File(filePathName.trim());
        return tempFile.getName();
    }

    private static boolean isE2007(String fileName) {
        return fileName.endsWith(EXCEL_EXT_2007);
    }

    private static Map<String,Sheet> getExcelSheets(InputStream input,boolean isE2007){
        Workbook wb = generateWorkbook(input,isE2007);
        Map<String,Sheet> map = Maps.newHashMap();
        int size =  wb.getNumberOfSheets();
        for (int i = 0; i < size; i++) {
            Sheet sheet = wb.getSheetAt(i);
            map.put(sheet.getSheetName(),sheet);
        }
        return map;
    }

    private static Workbook generateWorkbook(InputStream input,boolean isE2007){
        Workbook wb;
        try {
            //根据文件格式(2003或者2007)来初始化
            if (isE2007) {
                wb = new XSSFWorkbook(input);
            } else {
                wb = new HSSFWorkbook(input);
            }
        } catch (IOException e) {
            throw new RuntimeException();
        }
        return wb;
    }

    private static Iterator<Row> getExcelRows(InputStream input, Integer sheetNum, boolean isE2007) {
        Workbook wb = generateWorkbook(input,isE2007);
        Sheet sheet = wb.getSheetAt(sheetNum);     //获得第一个表单
        return sheet.rowIterator();
    }


}