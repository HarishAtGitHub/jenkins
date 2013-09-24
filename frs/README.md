This Plugin does the job of File synching between repository and another given location. 

It polls the repository for any change in file or any new file added or any existing file deleted . 

If any of the above happens then it immediately makes the corresponding change in the jenkins workspace .


Requirement that made me do this plugin : 
    To make automatic build trigger(here download and build) when the file's state changes in the repository .
    This plugin provides a simple mechanism to implement the above requirement.
    
    
Screen shot of the plugin configuration in jenkins : 

    
    
    
