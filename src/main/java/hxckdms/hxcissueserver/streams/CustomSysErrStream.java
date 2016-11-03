package hxckdms.hxcissueserver.streams;

import hxckdms.hxcissueserver.server.HxCIssueServer;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;

public class CustomSysErrStream extends PrintStream {
    public CustomSysErrStream(OutputStream out) {
        super(out);
    }

    @Override
    public void println(boolean x) {
        HxCIssueServer.logger.log(Level.WARNING, String.valueOf(x));
    }

    @Override
    public void println(char x) {
        HxCIssueServer.logger.log(Level.WARNING, String.valueOf(x));
    }

    @Override
    public void println(int x) {
        HxCIssueServer.logger.log(Level.WARNING, String.valueOf(x));
    }

    @Override
    public void println(long x) {
        HxCIssueServer.logger.log(Level.WARNING, String.valueOf(x));
    }

    @Override
    public void println(float x) {
        HxCIssueServer.logger.log(Level.WARNING, String.valueOf(x));
    }

    @Override
    public void println(double x) {
        HxCIssueServer.logger.log(Level.WARNING, String.valueOf(x));
    }

    @Override
    public void println(char[] x) {
        HxCIssueServer.logger.log(Level.WARNING, String.valueOf(x));
    }

    @Override
    public void println(String x) {
        HxCIssueServer.logger.log(Level.WARNING, x);
    }

    @Override
    public void println(Object x) {
        HxCIssueServer.logger.log(Level.WARNING, String.valueOf(x));
    }

    @Override
    public void print(boolean x) {
        HxCIssueServer.logger.log(Level.WARNING, String.valueOf(x));
    }

    @Override
    public void print(char x) {
        HxCIssueServer.logger.log(Level.WARNING, String.valueOf(x));
    }

    @Override
    public void print(int x) {
        HxCIssueServer.logger.log(Level.WARNING, String.valueOf(x));
    }

    @Override
    public void print(long x) {
        HxCIssueServer.logger.log(Level.WARNING, String.valueOf(x));
    }

    @Override
    public void print(float x) {
        HxCIssueServer.logger.log(Level.WARNING, String.valueOf(x));
    }

    @Override
    public void print(double x) {
        HxCIssueServer.logger.log(Level.WARNING, String.valueOf(x));
    }

    @Override
    public void print(char[] x) {
        HxCIssueServer.logger.log(Level.WARNING, String.valueOf(x));
    }

    @Override
    public void print(String x) {
        HxCIssueServer.logger.log(Level.WARNING, x);
    }

    @Override
    public void print(Object x) {
        HxCIssueServer.logger.log(Level.WARNING, String.valueOf(x));
    }
}
