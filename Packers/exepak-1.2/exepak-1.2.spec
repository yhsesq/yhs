Summary: exepak 1.2 compresses linux ELF executable files
Name: exepak
Version: 1.2
Release: 1
Copyright: GPL
Group: Utilities/File
Source: zurk.netpedia.net:/exepak-1.2.tar.gz
%description
This program allows Linux ELF executables to be compressed.
%prep
%setup
%build
./makeall
%install
cp exepak /bin
%files
%doc README license.txt HOW-TO-COMPILE ChangeLog
/bin/exepak

