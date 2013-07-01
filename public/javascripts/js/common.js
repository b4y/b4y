function verifyEmail(emailAddress){
	var status = false;     
	var emailRegEx = /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}$/i;
     if(emailAddress.search(emailRegEx) == -1) {
         //do nothing
     }else {
          status = true;
     }
     return status;
}

function verifyPassword(passOrig, passVerify){
	return (""!=passOrig) && (passOrig.indexOf(passVerify) == 0)
}