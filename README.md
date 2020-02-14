# WhiteHat Jenkins Plugin
Trigger WhiteHat scan from your jenkins build pipeline. 

Steps to build:
1. Checkout the repository.
2. Run mvn clean install -Dmaven.test.skip=true  
3. hpi file will be created inside target folder.

Installation step:
1. Click on Jenkins Menu --> Manage Jenkins --> Manage Plugin --> Advanced --> Choose File button and select hpi file located at target folder then press upload button.
2. To Reinstall first unistall via Jenkins Menu --> Manage Jenkins --> Manage Plugin --> Installed and then uninstall project and install plugin.
3. Now to access plugin go to Jenkins menu --> New Item. Provide item name. Select Freestyle project and click OK button.
4. Click on "Add post-build action" select WhiteHat Publisher. Provide required details and save it.
5. Press "Build Now" button


Copyright 2020 WhiteHat Security
