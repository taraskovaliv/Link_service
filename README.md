# Link_service - link shorter that store statistic of visiting it
---

## Installation

### 1. Create link_server database and user with password (PostgreSQL)
### 2. Create env file (link_server.env) with following variables:

Database credentials:
```dotenv
DB_URL=jdbc:postgresql://example.com:5432/link_service
DB_USERNAME=root
DB_PASSWORD=1111
```

Generated key that used as password to service:
```dotenv
MODERATION_KEY=iug34hf2348nrfdo4mtuir3w4oti32
```

Actual server domain or ip:
```dotenv
HOST=https://link.kovaliv.dev
```

Maxmind account id and licence key for getting location of user by ip address (optional), 
if you don't have it, you can register on [maxmind.com](https://www.maxmind.com/en/geolite2/signup) and get it in your account settings
```dotenv
#MAXMIND
MAXMIND_ACCOUNT_ID=
MAXMIND_LICENCE_KEY=
```

### 3. Run it using Docker:

```shell
docker compose up -d --build

```

## Endpoints

### / - home page to add links
#### Main page with adding link:
![Main page with adding link](/imgs/main_page.png)
#### Successful adding link page:
![Successful adding link page](/imgs/page_success.png)

### /statistic - page to see statistic of visiting links

#### Statistic page:
![Statistic page](/imgs/statistic_page.png)


## Contributing

Bug reports and pull requests are welcome on GitHub at https://github.com/taraskovaliv/Link_service.

## License

This lib is available as open source under the terms of the [MIT License](https://opensource.org/licenses/MIT).