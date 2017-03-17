# rss-server-epitech

Serveur d'aggréation RSS du module Java d'Epitech.

## API

Pour toutes les commandes vous enverrez dans un header HTTP `BEARER votre_token`.

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
Obtenir la liste des flux RSS

#### Response body

```json
{
  feeds: [{
      "url": "http://website/feed",
      "name": "Feed name",
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
