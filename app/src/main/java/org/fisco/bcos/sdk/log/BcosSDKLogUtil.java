package org.fisco.bcos.sdk.log;

import org.apache.log4j.Level;

import java.io.File;
import java.util.Locale;

/**
 * LogUtil 工具类
 *
 * @author Administrator
 */
@SuppressWarnings("all")
public class BcosSDKLogUtil {

    private static final String APP_NAME = "AndroidSDKDemo";

    /**
     * ### log文件的格式
     * <p>
     * ### 输出格式解释：
     * ### [%-d{yyyy-MM-dd HH:mm:ss}][Class: %c.%M(%F:%L)] %n[Level: %-5p] - Msg: %m%n
     * <p>
     * ### %d{yyyy-MM-dd HH:mm:ss}: 时间，大括号内是时间格式
     * ### %c: 全类名
     * ### %M: 调用的方法名称
     * ### %F:%L  类名:行号（在控制台可以追踪代码）
     * ### %n: 换行
     * ### %p: 日志级别，这里%-5p是指定的5个字符的日志名称，为的是格式整齐
     * ### %m: 日志信息
     * <p>
     * ### 输出的信息大概如下：
     * ### [时间{时间格式}][信息所在的class.method(className：lineNumber)] 换行
     * ### [Level: 5个字符的等级名称] - Msg: 输出信息 换行
     */
    private static final String LOG_FILE_PATTERN = "[%-d{yyyy-MM-dd HH:mm:ss}][Class: %c.%M(%F:%L)] %n[Level: %-5p] - Msg: %m%n";

    /**
     * 配置log4j参数
     */
    public static void configLog(String directory, Level logLevel) {

        LogConfig logConfig = new LogConfig();

        logConfig.setRootLevel(logLevel);

        String filePath = directory + File.separator + APP_NAME
                + File.separator + "Log" + File.separator + APP_NAME.toLowerCase(Locale.CHINA) + ".log";
        logConfig.setFileName(filePath);

        logConfig.setLevel("org.apache", Level.ERROR);

        logConfig.setFilePattern(LOG_FILE_PATTERN);

        logConfig.setMaxFileSize(1024 * 1024 * 5);

        logConfig.setImmediateFlush(true);

        logConfig.configure();

    }
}