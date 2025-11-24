# Getting Started

### Reference Documentation
```
curl -X POST "http://localhost:8080/api/customers/onboard" \
-H "Content-Type: multipart/form-data" \
-F 'CustomerOnboardRequest={"firstName":"John","lastName":"Doe","gender":"MALE","dateOfBirth":"1990-05-15","phoneNumber":"+31612345678","email":"john.doe@example.com","nationality":"NL","residentialAddress":"Damrak 1, 1012 LG Amsterdam","socialSecurityNumber":"123456782"};type=application/json' \
-F 'idProof=@"/Users/dr38io/Desktop/b.png"' \
-F 'photo=@"/Users/dr38io/Desktop/b.png"'
```


curl -X POST "http://localhost:8080/api/customers/onboard" \
-H "Content-Type: multipart/form-data" \
-F 'CustomerOnboardRequest={"firstName":"John","lastName":"Doe","gender":"MALE","dateOfBirth":"1990-05-15","phoneNumber":"+31612345678","email":"john.doe@example.com","nationality":"NL","residentialAddress":"Damrak 1, 1012 LG Amsterdam","socialSecurityNumber":"123456782"};type=application/json' \
-F 'idProof=@"/Users/dr38io/Desktop/b.png"' \
-F 'photo={"firstName":"John","lastName":"Doe","gender":"MALE","dateOfBirth":"1990-05-15","phoneNumber":"+31612345678","email":"john.doe@example.com","nationality":"NL","residentialAddress":"Damrak 1, 1012 LG Amsterdam","socialSecurityNumber":"123456782"};type=application/json' 