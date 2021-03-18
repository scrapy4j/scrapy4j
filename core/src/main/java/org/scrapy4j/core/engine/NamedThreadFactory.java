package org.scrapy4j.core.engine;

import java.util.concurrent.ThreadFactory;

public class NamedThreadFactory implements ThreadFactory {

    private final String prefix;
    private final ThreadGroup threadGroup;

    public NamedThreadFactory(ThreadGroup threadGroup,String prefix) {
        this.prefix = prefix;
        this.threadGroup=threadGroup;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(threadGroup,r, prefix + "-threadPool");
    }
}
