package net.collab.frs;

/*
        Aim : To download Files from Collabnet File Release System and this download is to
         be done periodically as per  duration specified in Poll SCM column of Build Trigger section of configure page.
         And immediately followed by build of it .

 */

// on taking descriptor impl
import hudson.scm.*;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import hudson.Extension;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;

//import on taking SCMRevisonstate and polling result
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.TaskListener;

//import to include method checkout -- unecessary
import hudson.model.BuildListener;

//import's for soap call part
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import javax.xml.rpc.ParameterMode;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.*;
import javax.activation.DataHandler;
import com.collabnet.ce.soap60.webservices.frs.FrsFileSoapDO;
import com.collabnet.ce.soap60.webservices.frs.FrsFileSoapRow;
import com.collabnet.ce.soap60.webservices.frs.FrsFileSoapList;



// import for polling
import java.util.*;
/**
 * Created by IntelliJ IDEA.
 * User: harish
 * Date: 4/2/12
 * Time: 12:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class FrsSynch extends SCM{

    private String url;
    private String relno ;
    private String frscusername ;
    private String frscpassword ;

    static InputStream in =null;
    static FileOutputStream out = null;
    static String separator  = File.separator;
    FilePath workspace;

    public FrsSynch(String url , String username ,String password, String relno ) throws IOException,ServiceException  {
        this.url = url;
        this.frscusername = username;
        this.frscpassword = password ;
        this.relno = relno;



        // in hudson 1.337, in filters = null, XStream will throw NullPointerException
        // this.filters = null;

    }
    public String getUrl() {
		return url;
	}
    public String getFrscusername() {
        return frscusername;
    }
    public String getFrscpassword() {
        return frscpassword;
    }

    public String getRelno() {
        return relno;
    }


    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Extension
    public static final class DescriptorImpl extends SCMDescriptor<FrsSynch> {

        public DescriptorImpl() {
            super(FrsSynch.class, null);
            load();
        }

        @Override
        public String getDisplayName() {
            return "Collabnet File Release System ";
        }



        @Override
        //har : global configuration if any can be put here and the corresponding global.jelly must be created
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {

            save();
            return super.configure(req,formData);
        }
        // : local configuration for each job  ,,has content when we save configuration page by clicking save
        @Override
        public FrsSynch newInstance(StaplerRequest req, JSONObject formData) throws FormException{
            String url = req.getParameter("frs.FileSystemURL");
            String frscusername = req.getParameter("frs.FileSystemUsername");
            String frscpassword = req.getParameter("frs.FileSystemPassword");
            String relno = req.getParameter("frs.FileSystemrelNo");


            try {
                return new FrsSynch(url, frscusername , frscpassword, relno ) ;
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ServiceException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
             // doubt
            return null;
        }


    }
     ///// Polling Logic
     public static void downloadFile(String url ,String workspace ,String filename , String ret1 , String ret2) throws ServiceException , IOException {
         String filenamesent = filename ;
         String sessionId = ret1 ;
         String downloadId = ret2 ;

         //*************** to download file ***********************

         String endpoint3 = url + "/ce-soap60/services/FileStorageApp";
         Service  service3 = new Service();
         Call call3 = (Call) service3.createCall();
         call3.setTargetEndpointAddress( new java.net.URL(endpoint3) );
         call3.setOperationName(new QName(endpoint3, "downloadFile"));
         call3.invoke( new Object[] { sessionId,downloadId } );
         DataHandler ret3 = (DataHandler) call3.invoke( new Object[] { sessionId,downloadId } );

         //************** downloaded file to be written to convenient form*********


         in = ret3.getInputStream();
         out = new FileOutputStream(workspace + separator + filenamesent);
         int c;
         byte[] buf = new byte[1024];
         while((c=in.read(buf))!=-1){

             out.write(buf,0,c);
         }
         in.close();
         out.close();

     }

     private boolean poll(AbstractProject<?, ?> project, Launcher launcher, FilePath workspace, TaskListener listener)
             throws IOException, InterruptedException {



         PrintStream log = launcher.getListener().getLogger();
         log.println("Polling started ");
         this.workspace = workspace;
         System.out.println(workspace);
         String dest = workspace.getRemote();
         File localfiles = new File(dest);

         try {


             //**********Goal: to get session ID*************************

             String endpoint1 =url + "/ce-soap60/services/CollabNet";
             Service  service1 = new Service();
             Call call1 = (Call) service1.createCall();
             call1.setTargetEndpointAddress( new java.net.URL(endpoint1) );
             call1.setOperationName(new QName(endpoint1, "login"));
             String ret1 = (String) call1.invoke( new Object[] { frscusername,frscpassword } );
             System.out.println(ret1);

             //*************Goal : to get number of files in a release ************ 

             String endpoint2 =url + "/ce-soap60/services/FrsApp";
             Service  service2 = new Service();
             Call call2 = (Call) service2.createCall();
             ///1) qName and mapping for FrsFileSoapRow 
             QName qList = new QName ("http://schema.open.collab.net/sfee50/soap60/type", "FrsFileSoapList");
             BeanSerializerFactory bsfList =   new BeanSerializerFactory(FrsFileSoapList.class, qList);
             BeanDeserializerFactory bdfList = new BeanDeserializerFactory(FrsFileSoapList.class, qList);
             call2.setTargetEndpointAddress( new java.net.URL(endpoint2) );
             call2.registerTypeMapping(FrsFileSoapList.class, qList, bsfList, bdfList);

             ///2) qName and mapping for FrsFileSoapRow 

             QName qRow = new QName ("http://schema.open.collab.net/sfee50/soap60/type", "FrsFileSoapRow");
             BeanSerializerFactory bsfRow =   new BeanSerializerFactory(FrsFileSoapRow.class, qRow);
             BeanDeserializerFactory bdfRow = new BeanDeserializerFactory(FrsFileSoapRow.class, qRow);
             call2.setTargetEndpointAddress( new java.net.URL(endpoint2) );
             call2.registerTypeMapping(FrsFileSoapRow.class, qRow, bsfRow, bdfRow);

             //3) Set operation name 
             call2.setOperationName(new QName(endpoint2, "getFrsFileList"));
             FrsFileSoapList r = (FrsFileSoapList) call2.invoke(new Object[] { ret1,relno } );



             // 4) get no of files in a release
             FrsFileSoapRow[] rows = r.getDataRows();
             int noOfRemoteFiles = rows.length;

             String[] ArrayremoteFilenames = new String[noOfRemoteFiles];
             System.out.println(noOfRemoteFiles);



             int downloadIndicator = 0 ;


             for( int i=1 ; i<=noOfRemoteFiles ; i++){
                 String fileid = rows[i-1].getId();

                 System.out.println(fileid);


                 // ***********Goal : to get file Data **********************
                 QName q = new QName ("http://schema.open.collab.net/sfee50/soap60/type", "FrsFileSoapDO");
                 BeanSerializerFactory bsf =   new BeanSerializerFactory(FrsFileSoapDO.class, q);
                 BeanDeserializerFactory bdf = new BeanDeserializerFactory(FrsFileSoapDO.class, q);
                 call2.setTargetEndpointAddress( new java.net.URL(endpoint2) );
                 call2.setOperationName(new QName(endpoint2, "getFrsFileId"));
                 String ret2 = (String) call2.invoke( new Object[] { ret1,fileid } );
                 System.out.println(ret2);

                 //************* to download data about the file *************

                 call2.registerTypeMapping(FrsFileSoapDO.class, q, bsf, bdf);
                 call2.setOperationName(new QName(endpoint2, "getFrsFileData"));
                 FrsFileSoapDO s = (FrsFileSoapDO) call2.invoke(new Object[] { ret1,fileid } );
                 System.out.println(s.getFilename());

                 Date lastModifiedDateOfRemote = s.getLastModifiedDate() ;
                 String filename = s.getFilename();

                 ArrayremoteFilenames[i-1] = filename;

                 String path = workspace + separator + filename ;
                 File pathToFile = new File(path);
                 if (!pathToFile.exists() ){
                     downloadFile(url ,dest ,filename , ret1 , ret2);
                     log.println("file "+ filename + " is being downloaded as it was newly loaded in repository ");
                     //return true;
                     downloadIndicator ++;
                 }
                 else {
                     long date = pathToFile.lastModified();
                     Date lastModifiedDateOfJWorkspace = new Date(date);
                     if (lastModifiedDateOfRemote.compareTo(lastModifiedDateOfJWorkspace) > 0){
                         downloadFile(url ,dest ,filename , ret1 , ret2);
                         log.println("file " + filename + " is being downloaded as the file was updated in repository");
                         //return true;
                         downloadIndicator ++;
                     }


                 }

             }
             // Goal : to delete extra files that has been deleted in box but still in jenkins workspace
             int noOfLocalFiles = localfiles.listFiles().length ;
             File[] localFileNames = localfiles.listFiles();
             String[] ArraylocalFilenames = new String[noOfLocalFiles];
             for (int j = 1; j<=noOfLocalFiles ; j++){
                    ArraylocalFilenames[j-1] = localFileNames[j-1].getName();
             }
             int indexOfFileToDelete ;
             if ( noOfLocalFiles > noOfRemoteFiles )  {
                 int indicator ;
                          for (int k = 0 ; k< noOfLocalFiles ; k++){
                              indicator = 0;
                              filematch :
                              for ( int l=0 ; l<noOfRemoteFiles ;l++ ){
                                  if (ArraylocalFilenames[k].equals(ArrayremoteFilenames[l])){
                                      indicator = indicator + 1;

                                      break filematch;
                                  }

                              }
                              if ( indicator==0){
                                  indexOfFileToDelete = k;
                                  String filepath = dest + separator + ArraylocalFilenames[indexOfFileToDelete] ;
                                  File filetodelete = new File(filepath) ;
                                  filetodelete.delete();
                                  log.println("file " + ArraylocalFilenames[indexOfFileToDelete] + "  is deleted from jenkins workspace as it was deleted in repository"); 
                              }
                          }

             }
             if (downloadIndicator>0){
             return true;
             }
             
             else {
                 return false ;

             }
         }
         catch (Exception e) {
             log.println("the polling failed because of the following error");
             log.println(e.toString());
             log.println();
             log.println();
             log.println();
             log.println();
             log.println("Suggested ways to correct the problem");
             log.println("Please check the url , password, and username given in configuration page");
             log.println("Please check if the license of the box where file system resides, has not expired ");
             log.println("Please check if the release number given is a valid one ");
             

             
             //System.err.println(e.toString());
         }
         finally {
             if (in != null) {
                 in.close();
             }
             if (out != null) {
                 out.close();
             }
         }




         return false;
     }

    // har : Polling Logic    // har : has content to be used when polling
    @Override
    protected PollingResult compareRemoteRevisionWith(
            AbstractProject<?, ?> project, Launcher launcher,
            FilePath workspace, TaskListener listener, SCMRevisionState baseline)
            throws IOException, InterruptedException {

        if(poll(project, launcher, workspace, listener)) {
            return PollingResult.SIGNIFICANT;
        } else {
            return PollingResult.NO_CHANGES;
        }

    }
    // unnecessary method
    @Override
    public SCMRevisionState calcRevisionsFromBuild(AbstractBuild<?, ?> build,
                                                   Launcher launcher, TaskListener listener) throws IOException,
            InterruptedException {
        // we cannot really calculate a sensible revision state for a filesystem folder
        // therefore we return NONE and simply ignore the baseline in compareRemoteRevisionWith
        return SCMRevisionState.NONE;
    }
    // unnecessary method
    // has content to be used when the build happens ,, that is when "Build now" button is clicked

    @Override
    public boolean checkout(AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener, File changelogFile)
            throws IOException, InterruptedException {
        
        
        return true;
    }
    // unnecessary method
    @Override
    public ChangeLogParser createChangeLogParser() {
        return null;
    }


}
