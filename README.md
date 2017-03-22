# rss-server-epitech

Serveur d'aggréation RSS du module Java d'Epitech.

## API

Pour toutes les commandes vous enverrez dans un header HTTP `Bearer votre_token`.

### POST /authorization/email
Obtenir un access_token avec l'email et le nom d'utilisateur.

#### Body (url encoded):

```
  email=adrien.morel@epitech.eu
  password=password
```

#### Response body

```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ"
}
```
### GET /feed
Obtenir un flux des feeds de l'utilisateur. Les articles sont classés par ordre chronologique du plus récent au plus aucien.

#### Response body

```json
{
  "items": [{
    "title": "Article's title",
    "description": "Article's description",
    "link": "Article's link",
    "feedName": "Feed name",
    "feedUrl": "http://website/feed",
    "date": "Wed, 22 Mar 2017 16:39:31 +0000"
  }]
}
```

### PUT /feed
Ajouter un feed

#### Body (url encoded):

```
url=http://website/feed
name=Feed name
```
### DELETE /feed
Supprimer un feed

#### Url parameters
```
url=http://website/feed
```
