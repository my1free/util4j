package com.my.util4j.excel.convert;

import com.google.common.collect.Lists;
import com.my.util4j.excel.annotation.ExcelCellField;
import com.my.util4j.excel.annotation.ExcelSheetField;
import com.my.util4j.excel.util.DateTimeUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * @author michealyang
 * @version 1.0
 * @created 18/6/11
 * 开始眼保健操： →_→  ↑_↑  ←_←  ↓_↓
 */
public class WorkBookConvert {

    public static <T> Workbook convert(T mutiSheetData){
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        Class<?> superClass = mutiSheetData.getClass();
        Field[] fields = superClass.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(ExcelSheetField.class)) {
                continue;
            }
            ExcelSheetField annotation = field.getAnnotation(ExcelSheetField.class);
            Sheet sheet = workbook.createSheet(annotation.name());
            Class<?> sheetClass = field.getType();
            //判断是Bean还是List
            List list = Lists.newArrayList();
            Type type = null;
            if(sheetClass.isAssignableFrom(List.class)){
                type = ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
                try {
                    list = (List<Object>) RowConvert.getMethod(field, superClass).invoke(mutiSheetData);
                } catch (Exception e) {
                    throw new RuntimeException(String.format("执行%s类,%s字段的get方法失败", superClass.getSimpleName(), field.getName()));
                }
            }else if(sheetClass.isPrimitive()){
                throw new RuntimeException(String.format("ExcelSheetField注解无法用于%s类,%s字段(基础类型)",superClass.getSimpleName(), field.getName()));
            }else{
                try {
                    list.add(RowConvert.getMethod(field, superClass).invoke(mutiSheetData));
                } catch (Exception e) {
                    throw new RuntimeException("转换异常");
                }
                type = field.getType();
            }
            //创建Tittle
            setHeader(sheet,workbook,TypeUtils.getRawType(type,null));
            setContent(sheet,workbook,list);
        }
        return workbook;
    }

    public static List<Sheet> getSheets(Workbook workbook, Class superClass) {
        Field[] fields = superClass.getFields();
        List<Sheet> sheets = Lists.newArrayList();
        for (Field field : fields) {
            if (field.isAnnotationPresent(ExcelSheetField.class)) {
                String sheetName = field.getAnnotation(ExcelSheetField.class).name();
                Sheet sheet = workbook.getSheet(sheetName);
                if (sheet == null) {
                    throw new RuntimeException(String.format("找不到名称为%s的Sheet", sheetName));
                }
                sheets.add(sheet);
            }
        }
        return sheets;
    }


    public static <T> Workbook convert(List<T> dataList) {
        // 创建excel工作簿
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();

        Class<?> clazz = dataList.get(0).getClass();
        setHeader(sheet, workbook, clazz);
        setContent(sheet, workbook, dataList);
        return workbook;
    }

    public static <T> Workbook convertInStream(List<T> dataList) {
        //默认窗口100
        Workbook workbook = new SXSSFWorkbook(100);
        Sheet sheet = workbook.createSheet();

        Class<?> clazz = dataList.get(0).getClass();
        setHeader(sheet, workbook, clazz);
        setContent(sheet, workbook, dataList);
        return workbook;
    }


    private static CellStyle setHeaderStyle(Workbook workbook) {
        // 创建单元格格式
        CellStyle headerStyle = workbook.createCellStyle();
        // 创建字体
        Font headerFont = workbook.createFont();
        // 创建字体样式（用于表头）
        headerFont.setFontHeightInPoints((short) 10);
        headerFont.setColor(IndexedColors.BLACK.getIndex());
        headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);

        // 设置单元格的样式（用于列名）
        headerStyle.setFont(headerFont);
        headerStyle.setBorderLeft(CellStyle.BORDER_THIN);
        headerStyle.setBorderRight(CellStyle.BORDER_THIN);
        headerStyle.setBorderTop(CellStyle.BORDER_THIN);
        headerStyle.setBorderBottom(CellStyle.BORDER_THIN);
        headerStyle.setAlignment(CellStyle.ALIGN_CENTER);
        return headerStyle;
    }

    private static <T> void setHeader(Sheet sheet, Workbook workbook, Class<T> clazz) {
        CellStyle headerStyle = setHeaderStyle(workbook);
        String[] indexNames = getIndexName(clazz);
        Row row0 = sheet.createRow(0);
        setRowValue(row0, headerStyle, indexNames);
    }

    private static void setRowValue(Row row, CellStyle headerStyle, String[] values) {
        for (int index = 0; index < values.length; index++) {
            Cell cell = row.createCell(index);
            CellConvert.setValue(cell, values[index]);
            cell.setCellStyle(headerStyle);
        }
    }

    private static <T> String[] getIndexName(Class<T> clazz) {
        Field[] fields = new Field[]{};
        Class<?> temp = clazz;
        while (temp != null) {//当父类为null的时候说明到达了最上层的父类(Object类).
            fields = ArrayUtils.addAll(temp.getDeclaredFields(),fields);
            temp = temp.getSuperclass();
        }
        String[] indexNames = new String[fields.length];

        //列集合
        List<Integer> indexList = Lists.newArrayList();
        for (Field field : fields) {
            if(!field.isAnnotationPresent(ExcelCellField.class)){
                continue;
            }
            ExcelCellField filedExcelAnnotation = field.getAnnotation(ExcelCellField.class);
            Integer index = filedExcelAnnotation.index();
            if (indexList.contains(index)) {
                throw new RuntimeException("设置的ExcelCellFiled.index出现重复,请进行调整!");
            }
            checkExcelAnnotationIndex(fields.length, index);
            indexNames[index] = filedExcelAnnotation.name();
            indexList.add(index);
        }

        return justIncludeExcelIndexValues(indexList, indexNames);
    }

    /**
     * 仅包含index的value
     * 排除掉数组中无用的空白信息,避免生成的Excel中最后列产生空白列.
     *
     * <p>
     * 例如:class UserErrorIndex {
     *
     * @ExcelCellField(name = "姓名", index = 0)
     * private String name;
     * @ExcelCellField(name = "年龄", index = 1)
     * private Integer age;
     * @ExcelCellField(name = "出生年月", index = 3, format = "yyyy-MM-dd")
     * private Date birthDay;
     * <p>
     * private String sex;
     * ...
     * }
     * <p>
     * 这样 indexList 为 0,1,3.indexValues数组为4(其中第四个是由sex属性产生的,但是没有设置sex需要排除)
     *
     *
     * @param indexList   列集合
     * @param indexValues 列值集合(可能存在无用信息,就是用户数据转化对象中,存在没有设置ExcelCellFiled的属性,会增加列数目.
     *                    需要排除,不排除,会增加Excel中的空白列)
     */
    private static String[] justIncludeExcelIndexValues(List<Integer> indexList, String[] indexValues) {
        //如果用户没有设置Excel那么就不会有数据了,返回吧
        String[] newIndexValues = new String[indexList.size()];

        if (CollectionUtils.isEmpty(indexList)) {
            throw new RuntimeException("数据中没有设置声明ExcelCellField,请设置后再使用此工具!");
        }
        for (int i = 0; i < indexList.size(); i++) {
            newIndexValues[i] = indexValues[indexList.get(i)];
        }
        return newIndexValues;
    }

    private static CellStyle setContentStyle(Workbook workbook) {
        CellStyle contentStyle = workbook.createCellStyle();

        Font contentFont = workbook.createFont();
        // 创建字体样式（用于值）
        contentFont.setFontHeightInPoints((short) 10);
        contentFont.setColor(IndexedColors.BLACK.getIndex());

        // 设置单元格的样式（用于值）
        contentStyle.setFont(contentFont);
        contentStyle.setBorderLeft(CellStyle.BORDER_THIN);
        contentStyle.setBorderRight(CellStyle.BORDER_THIN);
        contentStyle.setBorderTop(CellStyle.BORDER_THIN);
        contentStyle.setBorderBottom(CellStyle.BORDER_THIN);
        contentStyle.setAlignment(CellStyle.ALIGN_CENTER);
        return contentStyle;
    }

    private static <T> void setContent(Sheet sheet, Workbook workbook, List<T> dataList) {
        CellStyle contentStyle = setContentStyle(workbook);
        if (CollectionUtils.isNotEmpty(dataList)) {
            for (int i = 0; i < dataList.size(); ) {
                String[] indexValues = getIndexValue(dataList.get(i));
                Row row = sheet.createRow(++i);
                setRowValue(row, contentStyle, indexValues);
            }
        }
    }

    private static <T> String[] getIndexValue(T t) {
        Field[] fields = new Field[]{};
        Class<?> temp = t.getClass();
        while (temp != null) {//当父类为null的时候说明到达了最上层的父类(Object类).
            fields = ArrayUtils.addAll(temp.getDeclaredFields(),fields);
            temp = temp.getSuperclass();
        }
        String[] indexValues = new String[fields.length];


        //列集合
        List<Integer> indexList = Lists.newArrayList();

        for (Field field : fields) {
            ExcelCellField filedExcelAnnotation = RowConvert.getAnnotationCellFiled(field.getAnnotations());
            if (filedExcelAnnotation == null) {
                continue;
            }
            checkExcelAnnotationIndex(fields.length, filedExcelAnnotation.index());
            indexValues[filedExcelAnnotation.index()] = getValue(field, t, filedExcelAnnotation.format());
            indexList.add(filedExcelAnnotation.index());
        }
        return justIncludeExcelIndexValues(indexList, indexValues);
    }

    private static void checkExcelAnnotationIndex(int length, int index) {
        if (index > length) {
            throw new RuntimeException(String.format("Excel index=%s greater than length=%s", index, length));
        }
    }

    private static <T> String getValue(Field field, T t, String format) {
        Object value;
        try {
            value = PropertyUtils.getProperty(t, field.getName());
        } catch (Exception e) {
            throw new RuntimeException(String.format("can not getProperty: data=%s name=%s", t, field.getName()));
        }
        if (value == null) {
            return null;
        }
        Class<?> fieldType = field.getType();
        if (fieldType.isAssignableFrom(Date.class)) {
            Date date = (Date) value;
            String dateStr;
            try {
                dateStr = DateTimeUtils.convertDateToString(date, format);
            } catch (ParseException e) {
                throw new RuntimeException(String.format("can not convertDateToString: date=%s,format=%s", date, format));
            }
            return dateStr;
        }
        if (fieldType.isAssignableFrom(Boolean.class)) {
            Boolean isTrue = (Boolean) value;
            return isTrue ? "是" : "否";
        }
        return value.toString();
    }
}