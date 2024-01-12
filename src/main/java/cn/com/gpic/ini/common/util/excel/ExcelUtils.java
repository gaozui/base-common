package cn.com.gpic.ini.common.util.excel;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.excel.write.handler.WriteHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.ContentDisposition;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lzk&yjj
 */
public class ExcelUtils {

    private static final EnumMap<ExcelTypeEnum, String> EXCEL_CONTENT_TYPES = new EnumMap(ExcelTypeEnum.class);
    private static Collection<Converter> converters;
    private static Collection<ReadListener> readListeners;
    private static Collection<WriteHandler> writeHandlers;

    private ExcelUtils() {
        throw new AssertionError();
    }

    public static <T> List<T> read(File file, Class<T> head) {
        excelPropertyI18n(head);
        return read((ExcelReaderBuilder) ((ExcelReaderBuilder) EasyExcel.read(file).head(head)).ignoreEmptyRow(true).autoTrim(true));
    }

    public static <T> List<T> read(MultipartFile file, Class<T> head) {
        try {
            return read(file.getInputStream(), head);
        } catch (IOException var3) {
            throw new RuntimeException(var3.getMessage(), var3);
        }
    }

    public static <T> List<T> read(InputStream inputStream) {
        return read((InputStream) inputStream, (Class) null);
    }

    public static <T> List<T> read(InputStream inputStream, Class<T> head) {
        excelPropertyI18n(head);
        return read(((ExcelReaderBuilder) ((ExcelReaderBuilder) EasyExcel.read(IOUtils.buffer(inputStream)).head(head)).ignoreEmptyRow(true).autoTrim(true)).autoCloseStream(true));
    }

    public static <T> List<T> read(ExcelReaderBuilder builder) {
        getConverters().forEach(builder::registerConverter);
        getReadListeners().forEach(builder::registerReadListener);
        return builder.doReadAllSync();
    }

    public static <T> void write(File file, List<T> rows) {
        write((File) file, rows, (Class) (CollectionUtils.isEmpty(rows) ? null : rows.get(0).getClass()), (String) null);
    }

    public static <T> void write(File file, List<T> rows, Class<T> head) {
        write((File) file, rows, (Class) head, (String) null);
    }

    public static <T> void write(File file, List<T> rows, Class<T> head, String sheetName) {
        excelPropertyI18n(head);
        write(((ExcelWriterBuilder) ((ExcelWriterBuilder) EasyExcel.write(file).head(head)).excelType(getExcelType(file.getName())).autoTrim(true)).sheet(sheetName), rows, head);
    }

    public static <T> void write(HttpServletResponse response, List<T> rows, String fileName, Class<T> head) {
        write((HttpServletResponse) response, rows, (String) fileName, head, (String) null);
    }

    public static <T> void write(HttpServletResponse response, List<T> rows, String fileName, Class<T> head, String sheetName) {
        ServletOutputStream outputStream;
        try {
            outputStream = response.getOutputStream();
        } catch (IOException var7) {
            throw new RuntimeException(var7.getMessage(), var7);
        }

        ExcelTypeEnum excelType = getExcelType(fileName);
        response.setStatus(200);
        response.setContentType((String) EXCEL_CONTENT_TYPES.get(excelType));
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", ContentDisposition.builder("form-data").name("attachment").filename(fileName, StandardCharsets.UTF_8).build().toString());
        write((OutputStream) outputStream, rows, (ExcelTypeEnum) excelType, head, sheetName);
    }

    public static <T> void write(OutputStream outputStream, List<T> rows, ExcelTypeEnum excelType, Class<T> head) {
        write((OutputStream) outputStream, rows, (ExcelTypeEnum) excelType, head, (String) null);
    }

    public static <T> void write(OutputStream outputStream, List<T> rows, ExcelTypeEnum excelType, Class<T> head, String sheetName) {
        write(((ExcelWriterBuilder) ((ExcelWriterBuilder) EasyExcel.write(IOUtils.buffer(outputStream)).head(head)).excelType(excelType).autoTrim(true)).autoCloseStream(true).sheet(sheetName), rows, head);
    }

    public static <T> void write(ExcelWriterSheetBuilder builder, List<T> rows, Class<T> head) {
        excelPropertyI18n(head);
        getConverters().forEach(builder::registerConverter);
        getWriteHandlers().stream().peek((handler) -> {
        }).forEach(builder::registerWriteHandler);
        builder.doWrite((List) rows);
    }

    public static ExcelTypeEnum getExcelType(String fileName) {
        return (ExcelTypeEnum) EnumUtils.getEnumIgnoreCase(ExcelTypeEnum.class, FilenameUtils.getExtension(fileName), ExcelTypeEnum.XLSX);
    }

    private static void excelPropertyI18n(Class<?> head) {
        if (!Objects.isNull(head)) {
            ReflectionUtils.doWithLocalFields(head, (field) -> {
                if (!Objects.nonNull(AnnotationUtils.findAnnotation(field, ExcelIgnore.class))) {
                    ExcelProperty excelProperty = (ExcelProperty) AnnotationUtils.findAnnotation(field, ExcelProperty.class);
                    if (!Objects.isNull(excelProperty) && !StringUtils.isAllBlank(excelProperty.value())) {
                        InvocationHandler invocationHandler = Proxy.getInvocationHandler(excelProperty);
                        if (!Objects.isNull(invocationHandler)) {
                            Field memberValues = ReflectionUtils.findField(invocationHandler.getClass(), "memberValues");
                            if (!Objects.isNull(memberValues)) {
                                ReflectionUtils.makeAccessible(memberValues);
                                Map<String, Object> map = (Map) ReflectionUtils.getField(memberValues, invocationHandler);
                                if (!Objects.isNull(map)) {
                                    map.put("value", map.computeIfAbsent("value-backup", (key) -> excelProperty.value()));
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    public static Collection<Converter> getConverters() {
        if (Objects.isNull(converters)) {
            converters = (Collection) SpringUtil.getBeansOfType(Converter.class).values().stream().sorted(AnnotationAwareOrderComparator.INSTANCE).collect(Collectors.toList());
        }

        return converters;
    }

    public static Collection<ReadListener> getReadListeners() {
        if (Objects.isNull(readListeners)) {
            readListeners = (Collection) SpringUtil.getBeansOfType(ReadListener.class).values().stream().sorted(AnnotationAwareOrderComparator.INSTANCE).collect(Collectors.toList());
        }

        return readListeners;
    }

    public static Collection<WriteHandler> getWriteHandlers() {
        if (Objects.isNull(writeHandlers)) {
            writeHandlers = (Collection) SpringUtil.getBeansOfType(WriteHandler.class).values().stream().sorted(AnnotationAwareOrderComparator.INSTANCE).collect(Collectors.toList());
        }

        return writeHandlers;
    }

    static {
        EXCEL_CONTENT_TYPES.put(ExcelTypeEnum.XLS, "application/vnd.ms-excel");
        EXCEL_CONTENT_TYPES.put(ExcelTypeEnum.XLSX, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }
}
