package org.ifsoft.rdp.openfire;

import java.io.File;
import java.io.FileInputStream;
import java.net.*;
import java.util.concurrent.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import java.nio.file.*;

import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.http.HttpBindManager;
import org.jivesoftware.openfire.XMPPServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.PropertyEventDispatcher;
import org.jivesoftware.util.PropertyEventListener;

import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.servlets.*;
import org.eclipse.jetty.servlet.*;
import org.eclipse.jetty.webapp.WebAppContext;

import org.eclipse.jetty.util.security.*;
import org.eclipse.jetty.security.*;
import org.eclipse.jetty.security.authentication.*;

import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;

import java.lang.reflect.*;
import java.util.*;


public class PluginImpl implements Plugin, PropertyEventListener
{
    private static final Logger Log = LoggerFactory.getLogger(PluginImpl.class);

    private ServletContextHandler rdpContext;
    private ExecutorService executor;


    public void destroyPlugin()
    {
        PropertyEventDispatcher.removeListener(this);

        try {

            com.toremote.gateway.SparkGateway.stop();
            executor.shutdown();

            HttpBindManager.getInstance().removeJettyHandler(rdpContext);
        }
        catch (Exception e) {
            //Log.error("Rdp destroyPlugin ", e);
        }
    }

    public void initializePlugin(final PluginManager manager, final File pluginDirectory)
    {
        PropertyEventDispatcher.addListener(this);
        boolean rdpEnabled = JiveGlobals.getBooleanProperty("rdp.enabled", true);

        if (rdpEnabled)
        {
            executor = Executors.newCachedThreadPool();

            executor.submit(new Callable<Boolean>()
            {
                public Boolean call() throws Exception
                {
                    Log.info("Starting SparkGateway RDP service");

                    try {
                        String homeFolder = pluginDirectory.getAbsolutePath() + File.separator + "classes" + File.separator;
                        String openfireHome = JiveGlobals.getHomeDirectory() + File.separator;

                        File conf = new File(homeFolder + "gateway.conf");
                        Properties props = new Properties();
                        props.load(new FileInputStream(conf));

                        props.setProperty("html", homeFolder + "html");
                        props.setProperty("logfile", openfireHome + "logs" + File.separator + "rdp.log");

                        com.toremote.gateway.SparkGateway.init(props);
                        com.toremote.gateway.SparkGateway.start();

                        addRdpProxy();

                    } catch (Exception e) {
                        Log.error("rdp error", e);
                        Log.info("rdp disabled");
                    }

                    return true;
                }
            });

        } else {
            Log.info("rdp disabled");
        }
    }

    public String getIpAddress()
    {
        String ourHostname = XMPPServer.getInstance().getServerInfo().getHostname();
        String ourIpAddress = "127.0.0.1";

        try {
            ourIpAddress = InetAddress.getByName(ourHostname).getHostAddress();
        } catch (Exception e) {

        }

        return ourIpAddress;
    }

    private void addRdpProxy()
    {
        Log.info("Initialize RdpProxy");

        rdpContext = new ServletContextHandler(null, "/rdp", ServletContextHandler.SESSIONS);
        //rdpContext.setClassLoader(this.getClass().getClassLoader());

        ServletHolder proxyServlet = new ServletHolder(ProxyServlet.Transparent.class);
        proxyServlet.setInitParameter("proxyTo", "http://" + getIpAddress());
        proxyServlet.setInitParameter("prefix", "/");
        rdpContext.addServlet(proxyServlet, "/*");

        HttpBindManager.getInstance().addJettyHandler(rdpContext);
    }

//-------------------------------------------------------
//
//
//
//-------------------------------------------------------


    public void propertySet(String property, Map params)
    {

    }

    public void propertyDeleted(String property, Map<String, Object> params)
    {

    }

    public void xmlPropertySet(String property, Map<String, Object> params) {

    }

    public void xmlPropertyDeleted(String property, Map<String, Object> params) {

    }

}
