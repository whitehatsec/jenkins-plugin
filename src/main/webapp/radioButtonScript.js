function radioSelected() {
 		var r = document.getElementsByName("serverSelected");
 		for(i=0; i < r.length;i++) {
 			if(r[i].checked) {
 				alert (r[i].value + "Jigar") ;
 				return r[i];
 			}
 			return null;
 		}
 	}