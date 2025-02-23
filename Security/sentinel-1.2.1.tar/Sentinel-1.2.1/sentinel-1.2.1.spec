Summary: Sentinel is a file/drive integrity checking tool
Name: Sentinel 
Version: 1.2.1
Release: 1
Copyright: GPL
Group: Utilities/Console 
Source0: zurk.sourceforge.net:/sentinel-1.2.1.tar.gz 
%description 
Sentinel is a fast file scanner similar to Tripwire or Viper with built in authentication using the RIPEMD 160 bit MAC hashing
function. It uses a single database similar to Tripwire, maintains file integrity using the RIPEMD algorithm and also produces secure,
signed logfiles. Its main design goal is to detect intruders modifying files. It also prevents intruders with root/superuser
permissions from tampering with its log files and database. Available versions are for linux (tested on all current Slackware and
RedHat releases), with Irix versions soon to be added on.
%prep
%setup
%build
mkdir /opt/sentinel
./makeall
%install
cp sentinel /opt/sentinel
cp gsentinel /opt/sentinel
ln -s /opt/sentinel/gsentinel /bin/sentinel
cp sentinel.conf /opt/sentinel
cp sentineld /opt/sentinel
cp README.sentinel /opt/sentinel
%files
%doc README.sentinel readme.gsentinel readme.sentineld

/opt/sentinel/sentinel
/opt/sentinel/sentinel.conf
/opt/sentinel/gsentinel
/opt/sentinel/sentineld
/bin/sentinel
