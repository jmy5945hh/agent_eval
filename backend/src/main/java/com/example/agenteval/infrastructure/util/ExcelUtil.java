package com.example.agenteval.infrastructure.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;

import java.util.List;

/**
 * excel工具类
 */
public class ExcelUtil {

    /**
     * 写excel
     *
     * @param filePath      文件路径
     * @param sheetName     第一个sheet页名字
     * @param excelDataList excel中的数据
     * @param excelClass    excel填充实体类
     * @param <T>
     */
    public static <T> void writeExcel(String filePath, String sheetName, List<T> excelDataList, Class<T> excelClass) {
        try (ExcelWriter excelWriter = EasyExcel.write(filePath).build()) {
            WriteSheet sheet = EasyExcel.writerSheet(0, sheetName).head(excelClass)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()).build();
            excelWriter.write(excelDataList, sheet);
            excelWriter.finish();
        }
    }

}
