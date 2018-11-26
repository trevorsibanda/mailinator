Mailinator service. 

Support for both heap and sql backend
Tested with up to 1,000,000 emails distributed across 500 users.
    - Tested on Inter Core i7 8th gen with 8GB RAM
  
- Steps to run

```bash
sbt compile
sbt run
```
- Active Endpoints

POST /mailboxes
Creates a new random address

```json
{address: "", when: ""}
```

POST /mailboxes/:address/messages
Creates a new email and stores it in `address` mailbox
Request
```json
{from: "", subject: "", body: ""}
```
Response
```json
{id: "", when: ""}
```

GET /mailboxes/:address/messages
Gets all emails in :address's mailbox
```json
{cursor:{prev: 1, next: null}, count: 0, results: []}
```

GET /mailboxes/:address/messages/:cursor/:count

DELETE /mailboxes/:address

DELETE /mailboxes/:address/messages/:id


