package com.tnedicca.routewise.classes;

import android.content.Context;
import android.util.Log;


import com.tnedicca.routewise.BuildConfig;
import com.tnedicca.routewise.activities.RouteWise;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.File;

import de.mindpipe.android.logging.log4j.LogConfigurator;

/**
 * Created by Aachu on 22-03-2017.
 */

public class RouteLog {
    public Logger logger;

    public RouteLog() {
    }

    public void setLoggerClass(Class<?> name) {
        String fileName = name.getSimpleName();
        logger = Logger.getLogger(fileName);
    }

    public void configureLog() {
//        LogConfigurator logConfigurator = new LogConfigurator();
//        File file = new File(AppConstant.KEY_LOG_FILE_PATH);
//        if (!file.exists())
//            file.mkdirs();res
//        logConfigurator.setFileName(AppConstant.KEY_LOG_FILE_PATH + File.separator + AppConstant.LOG_FILE_NAME);
//        logConfigurator.setRootLevel(Level.DEBUG);
//        logConfigurator.setLevel("org.apache", Level.ERROR);
//        logConfigurator.setFilePattern("%d %-5p [%c{2}]-[%L] %m%n");
//        logConfigurator.setMaxFileSize(1024 * 1024 * 5);// 5MB
//        logConfigurator.setImmediateFlush(true);
//        logConfigurator.configure();

        // creates pattern layout
        PatternLayout layout = new PatternLayout();
        String conversionPattern = "[%p] %d %c %M - %m%n";
        layout.setConversionPattern(conversionPattern);

        // creates daily rolling file appender
        DailyRollingFileAppender rollingAppender = new DailyRollingFileAppender();
        rollingAppender.setFile(AppConstant.KEY_LOG_FILE_PATH + File.separator + AppConstant.LOG_FILE_NAME);
        rollingAppender.setDatePattern("'.'yyyy-MM-dd");
        rollingAppender.setLayout(layout);
        rollingAppender.activateOptions();

        // configures the root logger
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.DEBUG);
        rootLogger.addAppender(rollingAppender);
    }

    public void info(String message) {
        info(message, null);
    }

    public void info(String message, Throwable t) {
        logger.info(message, t);
        if (!BuildConfig.RELEASE) {
            Log.i(AppConstant.DEFAULT_DB_NAME, message);
        }
    }

    public void error(String message) {
        error(message, null);
    }

    public void error(String message, Throwable t) {
        logger.error(message, t);
    }

}
