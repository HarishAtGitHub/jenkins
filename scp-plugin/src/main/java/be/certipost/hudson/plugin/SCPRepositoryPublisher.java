package be.certipost.hudson.plugin;

import com.jcraft.jsch.*;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.model.Result;
import hudson.model.Hudson.MasterComputer;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.CopyOnWriteList;
import hudson.util.DescribableList;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import hudson.util.IOException2;
import hudson.util.ListBoxModel;
import hudson.util.ListBoxModel.Option;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * 
 * @author Ramil Israfilov
 * 
 */
public final class SCPRepositoryPublisher extends Notifier {

	/**
	 * Name of the scp site to post a file to.
	 */
	private String siteName;
	public static final Logger LOGGER = Logger
			.getLogger(SCPRepositoryPublisher.class.getName());

    ////////////////////////////////////    start
    private String hostname;
    private int port;
    private String rootRepositoryPath = "/";
    // as i want to remove this factor called rootrepositorypath from UI, but at the same not disturb the
    //logic , i want to hard code  rootRepositoryPath to "/"
    private String scpUsername;
    private String scpPassword;
    private String keyfile;
    private String source;
    private String destination;
    private Boolean keepHierarchy;


    ////////////////////////////////////////end



    /////////////////////////////// start
    public SCPRepositoryPublisher(String hostname ,int port,String scpUsername,String scpPassword,String keyfile,String source,String destination,boolean keepHierarchy ) throws IOException {
        this.hostname = hostname;
        this.port = port;
       // this.rootRepositoryPath = rootRepositoryPath ;
        // as i want to remove this factor called rootrepositorypath but at the same not disturb the
        //logic , i want to hard code  rootRepositoryPath to "/"
        this.scpUsername = scpUsername;
        this.scpPassword = scpPassword;
        this.keyfile = keyfile;
        this.source = source ;
        this.destination = destination;
        this.keepHierarchy = keepHierarchy;




        // in hudson 1.337, in filters = null, XStream will throw NullPointerException
        // this.filters = null;

    }
    public String getKeyfile() {
        return keyfile;
    }

    public void setKeyfile(String keyfile) {
        this.keyfile = keyfile;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getPort() {
        return "" + port;
    }

    public void setPort(String port) {
        try {
            this.port = Integer.parseInt(port);
        } catch (Exception e) {
            this.port = 22;
        }
    }

    public int getIntegerPort() {
        return port;
    }

    public String getScpUsername() {
        return scpUsername;
    }

    public void setScpUsername(String scpUsername) {
        this.scpUsername = scpUsername;
    }

    public String getScpPassword() {
        return scpPassword;
    }

    public void setScpPassword(String scpPassword) {
        this.scpPassword = scpPassword;
    }
    /*
    public String getRootRepositoryPath() {
        return rootRepositoryPath;
    }
        */
    /*
    public void setRootRepositoryPath(String rootRepositoryPath) {
        this.rootRepositoryPath = rootRepositoryPath.trim();
    }
        */
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source ;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Boolean getKeepHierarchy(){
        return keepHierarchy;
    }
    public void setKeepHierarchy(boolean keephierarchy){
        this.keepHierarchy = keephierarchy;
    }


     ////////////////////////////end

     /*
	private final List<Entry> entries;

    @DataBoundConstructor
    public SCPRepositoryPublisher(String siteName, List<Entry> entries) {
        if (siteName == null) {
            // defaults to the first one
            SCPSite[] sites = DESCRIPTOR.getSites();
			if (sites.length > 0) {
                siteName = sites[0].getName();    //username + "@" + hostname + ":" + rootRepositoryPath  or the model display name
            }
        }
        this.entries = entries;
        this.siteName = siteName;//username + "@" + hostname + ":" + rootRepositoryPath  or the model display name
    }

	public List<Entry> getEntries() {
		return entries;
	}

	public SCPSite getSite() {
		SCPSite[] sites = DESCRIPTOR.getSites();
        // if no site name is selected in the job config page
		if (siteName == null && sites.length > 0)
			// default
			return sites[0];
        // if  a site name is selected in the job config page
        // getting that site bean(this has the data we put in the global config page) , that corresponds to the site name selected
		for (SCPSite site : sites) {
			if (site.getName().equals(siteName))
				return site;
		}
		return null;
	}
          */
    /// unnecessary
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.BUILD;
	}

	/**
	 * Returns the environment variables set for a node/slave. So you can use
	 * them, as are in your environment
	 *     envVars    comes    after  @param
	 * @param
	 * @return
	 */

	public static EnvVars getEnvVars() {
		DescribableList<NodeProperty<?>, NodePropertyDescriptor> nodeProperties = getNodeProperties();

		// System.out.println(".getEnvVars()Computer.currentComputer() = "+Computer.currentComputer()+
		// "nodeProperties = "+nodeProperties);

		Iterator<NodeProperty<?>> iterator = nodeProperties.iterator();
		while (iterator.hasNext()) {
			NodeProperty<?> next = iterator.next();
			// System.out.println(".getEnvVars()Computer.currentComputer() = "+Computer.currentComputer()+" next = "
			// + next);
			if (next instanceof EnvironmentVariablesNodeProperty) {
				EnvironmentVariablesNodeProperty envVarProp = (EnvironmentVariablesNodeProperty) next;
				EnvVars envVars = envVarProp.getEnvVars();
				return envVars;
			}

		}
		return null;
	}

	private static DescribableList<NodeProperty<?>, NodePropertyDescriptor> getNodeProperties() {
		Node node = Computer.currentComputer().getNode();
		DescribableList<NodeProperty<?>, NodePropertyDescriptor> nodeProperties = node
				.getNodeProperties();

		if (Computer.currentComputer() instanceof MasterComputer) {
			Hudson instance = Hudson.getInstance();
			nodeProperties = instance.getGlobalNodeProperties();
		}
		return nodeProperties;
	}

    public Session createSession(PrintStream logger) throws JSchException {
        JSch jsch = new JSch();

        Session session = jsch.getSession(scpUsername, hostname, port);
        if (this.keyfile != null && this.keyfile.length() > 0) {
            jsch.addIdentity(this.keyfile, this.scpPassword);
        } else {
            session.setPassword(scpPassword);
        }

        UserInfo ui = new SCPUserInfo(scpPassword);
        session.setUserInfo(ui);

        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        log(logger,"session successfully created");
        return session;

    }
    public void closeSession(PrintStream logger, Session session,
                             ChannelSftp channel) {
        if (channel != null) {
            channel.disconnect();
            channel = null;
        }
        if (session != null) {
            session.disconnect();
            session = null;
        }
        log(logger,"session successfully closed");

    }

    public ChannelSftp createChannel(PrintStream logger, Session session)
            throws JSchException {
        ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
        channel.setOutputStream(System.out);
        channel.connect();
        log(logger,"channel successfully created");
        return channel;
    }

    private void mkdirs(String filePath, PrintStream logger, ChannelSftp channel)
            throws SftpException, IOException {
        String pathnames[] = filePath.split("/");
        String curdir = rootRepositoryPath;
        if (pathnames != null) {
            for (int i = 0; i < pathnames.length; i++) {
                if (pathnames[i].length() == 0) {
                    continue;
                }

                SftpATTRS dirstat = null;
                try {
                    dirstat = channel.stat(curdir + "/" + pathnames[i]);
                } catch (SftpException e) {

                    if (e.getMessage() != null
                            && e.getMessage().indexOf("No such file") == -1) {
                        log(logger, "Error getting stat of  directory:"
                                + curdir + "/" + pathnames[i] + ":"
                                + e.getMessage());
                        throw e;
                    }
                }
                if (dirstat == null) {
                    // try to create dir
                    log(logger, "Trying to create " + curdir + "/"
                            + pathnames[i]);
                    channel.mkdir(curdir + "/" + pathnames[i]);
                } else {
                    if (!dirstat.isDir()) {
                        throw new IOException(curdir + "/" + pathnames[i]
                                + " is not a directory:" + dirstat);
                    }
                }
                curdir = curdir + "/" + pathnames[i];
            }
        }
    }

    private String concatDir(String folderPath, String strRelativePath) {
        String strTmp;
        if (folderPath.endsWith("/") || folderPath.equals("")) {
            strTmp = folderPath + strRelativePath;
        } else {
            strTmp = folderPath + "/" + strRelativePath;
        }

        // System.out.println("SCPSite.concatDir()strTmp = " + strTmp);

        return strTmp;
    }

    private String extractRelativePath(String strWorkspacePath,
                                       FilePath filePath, PrintStream logger) {
        String strRet = "";
        String strFilePath = filePath.getParent().toString();
        if (strWorkspacePath.length() == strFilePath.length()) {
            return "";
        }

        if (strFilePath.length() > strWorkspacePath.length()) {
            strRet = strFilePath.substring(strWorkspacePath.length() + 1,
                    strFilePath.length());// Exclude
            // first
            // file
            // separator
        }
        strRet = strRet.replace('\\', '/');

        return strRet;
    }

    public void upload(String folderPath, FilePath filePath, boolean keepHierarchy,
                       Map<String, String> envVars, PrintStream logger, ChannelSftp channel)
            throws IOException, InterruptedException, SftpException {
        // if ( session == null ||
        if (channel == null) {
            throw new IOException("Connection to " + hostname + ", user="
                    + scpUsername + " is not established");
        }
        SftpATTRS rootdirstat = channel.stat(rootRepositoryPath);
        if (rootdirstat == null) {
            throw new IOException(
                    "Can't get stat of root repository directory:"
                            + rootRepositoryPath);
        } else {
            if (!rootdirstat.isDir()) {
                throw new IOException(rootRepositoryPath
                        + " is not a directory");
            }
        }
        if (filePath.isDirectory()) {
            FilePath[] subfiles = filePath.list("**/*");
            if (subfiles != null) {
                for (int i = 0; i < subfiles.length; i++) {
                    upload(folderPath + "/" + filePath.getName(), subfiles[i],
                            keepHierarchy, envVars, logger, channel);
                }
            }
        } else {
            String localfilename = filePath.getName();
            mkdirs(folderPath, logger, channel);

            String strNewFilename;
            //String dest ;
            if (keepHierarchy) {
                // Fix for mkdirs
                String strWorkspacePath = envVars.get("strWorkspacePath");
                String strRelativePath = extractRelativePath(strWorkspacePath,
                        filePath, logger);

                String strTmp = concatDir(folderPath, strRelativePath);
                String strNewPath = concatDir(rootRepositoryPath, strTmp);
                if (!strRelativePath.equals("")) {
                    // System.out.println("SCPSite.upload()   mkdirs(strTmp = "
                    // + strTmp);
                    // Make subdirs
                    mkdirs(strTmp, logger, channel);
                }

                if (!strNewPath.endsWith("/")) {
                    strNewPath += "/";
                    
                }

                strNewFilename = strNewPath + localfilename;
                //dest = strNewFilename.substring(1) ;
            } else {
                String strTmp = concatDir(folderPath, localfilename);
                strNewFilename = concatDir(rootRepositoryPath, strTmp);
                //dest = strNewFilename.substring(1);
            }

            log(logger, "uploading file: '" + localfilename );
            InputStream in = filePath.read();
            channel.put(in, strNewFilename);
            // ~Fix for mkdirs

            in.close();
        }

    }
    
	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {

		if (build.getResult() == Result.FAILURE) {
			// build failed. don't post
			return true;
		}

		//SCPSite scpsite = null;
		PrintStream logger = listener.getLogger();
		Session session = null;
		ChannelSftp channel = null;
		try {
			//har : scpsite = getSite();
			//har if (scpsite == null) {
            if( hostname==null) {
				log(logger,
						"No SCP site is configured. This is likely a configuration problem.");
				build.setResult(Result.UNSTABLE);
				return true;
			}
			//log(logger, "Connecting to " + scpsite.getHostname());
            log(logger, "Connecting to " + hostname);
///////////******************************************
            // session = scpsite.createSession(logger);
			session = createSession(logger);


///////////******************************************
			//channel = scpsite.createChannel(logger, session);
            channel = createChannel(logger, session);
            
            
			Map<String, String> envVars = build.getEnvironment(listener);
			// Patched for env vars
			EnvVars objNodeEnvVars = getEnvVars();
			if (objNodeEnvVars != null) {
				envVars.putAll(objNodeEnvVars);
			}
			// ~ Patched for env vars
            //Entry e = new Entry();

			// har : for (Entry e : entries) {
				String expanded = Util.replaceMacro(source, envVars);
				FilePath ws = build.getWorkspace();
				FilePath[] src = ws.list(expanded);
				if (src.length == 0) {
					// try to do error diagnostics
					log(logger, ("No file(s) found: " + expanded));
					String error = ws.validateAntFileMask(expanded);
					if (error != null)
						log(logger, error);

					//continue;
				}
				String folderPath = Util.replaceMacro(destination, envVars);

				// Fix for recursive mkdirs
				folderPath = folderPath.trim();

				// Making workspace to have the same path separators like in the
				// FilePath objects
				String strWorkspacePath = ws.toString();

				String strFirstFile = src[0].toString();
				if (strFirstFile.indexOf('\\') >= 0) {
					strWorkspacePath = strWorkspacePath.replace('/', '\\');
				} else {
					strWorkspacePath = strWorkspacePath.replace('\\', '/');// Linux
					// Unix
				}

				// System.out
				// .println("SCPRepositoryPublisher.perform()strWorkspacePath = "
				// + strWorkspacePath);
				envVars.put("strWorkspacePath", strWorkspacePath);
				// ~Fix for recursive mkdirs

				if (src.length == 1) {
					// log(logger, "remote folderPath '" + folderPath
					// + "',local file:'" + src[0].getName() + "'");
					// System.out.println("remote folderPath '" + folderPath
					// + "',local file:'" + src[0].getName() + "'");
///////////******************************************
					//scpsite.upload(folderPath, src[0], e.keepHierarchy, envVars, logger, channel);
                    log(logger, ("about to download " ));
                    upload(folderPath, src[0], keepHierarchy, envVars, logger, channel);
                } else {
					for (FilePath s : src) {
						// System.out.println("remote folderPath '" + folderPath
						// + "',local file:'" + s.getName() + "'");
						// log(logger, "remote folderPath '" + folderPath
						// + "',local file:'" + s.getName() + "'");
///////////******************************************
						//scpsite.upload(folderPath, s, e.keepHierarchy, envVars, logger, channel);
                        log(logger, ("about to download " ));
                        upload(folderPath, s, keepHierarchy, envVars, logger, channel);
					}
				}
			//}
		} catch (IOException e) {
			e.printStackTrace(listener.error("Failed to upload files"));
			build.setResult(Result.UNSTABLE);
		} catch (JSchException e) {
			e.printStackTrace(listener.error("Failed to upload files"));
			build.setResult(Result.UNSTABLE);
		} catch (SftpException e) {
			e.printStackTrace(listener.error("Failed to upload files"));
			build.setResult(Result.UNSTABLE);
		} finally {

			//if (scpsite != null) {
            if(hostname != null){
///////////******************************************
				closeSession(logger, session, channel);
			}

		}

		return true;
	}
    ////////////////////////////////////////////////////////////////////
	@Extension
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	public static final class DescriptorImpl extends
			BuildStepDescriptor<Publisher> {

		public DescriptorImpl() {
			super(SCPRepositoryPublisher.class);
			load();
		}

		protected DescriptorImpl(Class<? extends Publisher> clazz) {
			super(clazz);
		}





         //1
        /*  har
		private final CopyOnWriteList<SCPSite> sites = new CopyOnWriteList<SCPSite>();
              */
		public String getDisplayName() {
			// har : return Messages.SCPRepositoryPublisher_DisplayName();
            return "Collabnet Secure Copy(SCP) ";
		}

		public String getShortName() {
			return "[SCP] ";
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}
        //2
        /*
		public SCPSite[] getSites() {
			Iterator<SCPSite> it = sites.iterator();
			int size = 0;
			while (it.hasNext()) {
				it.next();
				size++;
			}
			return sites.toArray(new SCPSite[size]);
		}
		      */

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData) {
			//har : sites.replaceBy(req.bindParametersToList(SCPSite.class, "scp."));
			save();
			return true;
		}
         /**har
        public ListBoxModel doFillSiteNameItems() {
            ListBoxModel model = new ListBoxModel();
            for (SCPSite site : getSites()) {
                model.add(site.getName());
            }
            return model;
        }
        **/
         @Override
         public SCPRepositoryPublisher newInstance(StaplerRequest req, JSONObject formData) throws FormException{
             String hostname = req.getParameter("scp.Hostname");
             int port = Integer.valueOf(req.getParameter("scp.Port"))  ;
           //  String rootRepositoryPath = req.getParameter("scp.RootRepositoryPath");
             String scpUsername = req.getParameter("scp.scpUsername");
             String scpPassword = req.getParameter("scp.scpPassword");
             String keyfile = req.getParameter("scp.Keyfile");
             String source = req.getParameter("scp.Source");
             String destination = req.getParameter("scp.Destination");
             //boolean keepHierarchy = req.getParameter("scp.keepHierarchy");
             Boolean keepHierarchy = Boolean.valueOf("on".equalsIgnoreCase(req.getParameter("scp.keepHierarchy")));

             try {
                 return new SCPRepositoryPublisher(hostname , port,scpUsername, scpPassword,keyfile,source,destination,keepHierarchy ) ;
             } catch (IOException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
             // doubt
             return null;
         }
	}

    /*
	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}
        */
	protected void log(final PrintStream logger, final String message) {
		logger.println(StringUtils.defaultString(DESCRIPTOR.getShortName())
				+ message);
	}
}
