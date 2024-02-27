
# Networking_p2_Varano


Instructions: 
Run SinkholeServer.java 

Usage:
sinkhole --config <config>            Configure the Sinkhole settings.
sinkhole --help                       Display the help.
sinkhole --start                      Start the sinkhole.

Enter your option: [ ]

Option Information:
- --config <config>: Allows you to specify the configuration file for the Sinkhole.
- --help: Displays help information about Sinkhole usage.
- --start: Initiates the Sinkhole server.

Choose an option:
1. Set Config File                   - Specify the configuration file.
2. Choose Port                       - Set the port number for the sinkhole.
3. Change DNS Address                - Change the DNS address for the sinkhole.
4. Add Block Site                    - Add a website to block.
5. Back to main menu                 - Return to the main menu.

Enter your choice: [ ]

Option Information:
- Option 1: Allows you to specify the configuration file for the Sinkhole.
- Option 2: Enables you to set the port number for the Sinkhole server.
- Option 3: Allows you to change the DNS address used by the Sinkhole server.
- Option 4: Enables you to add a website to the list of blocked sites.
- Option 5: Returns you to the main menu.



# Sample lookups: 


google type a: nslookup -port=5000 google.com 127.0.0.1

google type aaaa: nslookup -port=5000 -query=AAAA google.com 127.0.0.1


google type aaaa: nslookup -port=5000 -query=AAAA youtube.com 127.0.0.1


https://www.lifewire.com/free-and-public-dns-servers-2626062