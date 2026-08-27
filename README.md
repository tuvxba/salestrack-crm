# SalesTrack CRM

SalesTrack CRM, satış ekiplerinin müşteri adaylarını, firmaları, kişileri, fırsatları ve aktiviteleri tek yerden yönetmesine yardımcı olan RESTful bir CRM API'sidir.

Uygulama; JWT tabanlı kimlik doğrulama, rol bazlı yetkilendirme, satış hunisi takibi ve raporlama özelliklerini içerir.

## Özellikler

- Kullanıcı kaydı, giriş işlemi ve JWT ile oturum yönetimi
- Rol tabanlı yetkilendirme
- Firma ve ilgili kişi yönetimi
- Lead (müşteri adayı) oluşturma, durum takibi ve dönüştürme
- Deal (satış fırsatı) yönetimi ve aşama geçişleri
- Arama, toplantı, e-posta gibi satış aktivitelerinin kaydı
- Satış hunisi, dönüşüm oranı, kazanılan fırsatlar ve kullanıcı performansı raporları
- Flyway ile sürümlü veritabanı migrasyonları
- OpenAPI/Swagger ile etkileşimli API dokümantasyonu
- Docker Compose ile PostgreSQL üzerinden hızlı yerel kurulum

## Teknolojiler

- Java ve Spring Boot
- Spring Security / JWT
- Spring Data JPA (Hibernate)
- PostgreSQL
- Flyway
- Maven
- Docker ve Docker Compose
- springdoc-openapi (Swagger UI)

## Proje Yapısı

```text
src/main/java/com/salestrack
├── config/        # Uygulama, güvenlik ve OpenAPI yapılandırmaları
├── controller/    # REST uç noktaları
├── dto/           # İstek ve yanıt modelleri
├── entity/        # JPA varlıkları
├── enums/         # Lead, deal, aktivite ve rol enum'ları
├── exception/     # Merkezi hata yönetimi
├── mapper/        # Entity/DTO dönüşümleri
├── repository/    # Veri erişim katmanı
├── security/      # JWT ve kullanıcı doğrulama bileşenleri
└── service/       # İş kuralları

src/main/resources/db/migration/  # Flyway SQL migrasyonları
```

## Gereksinimler

Yerel olarak çalıştırmak için aşağıdakilerden biri yeterlidir:

- Java (projedeki Maven yapılandırmasının gerektirdiği sürüm)
- Docker Desktop ve Docker Compose

Projede Maven Wrapper bulunduğu için sisteminizde ayrıca Maven kurulu olması gerekmez.

## Hızlı Başlangıç

### Docker ile çalıştırma

```bash
docker compose up --build
```

Arka planda çalıştırmak için:

```bash
docker compose up --build -d
```

Servisleri durdurmak için:

```bash
docker compose down
```

### Yerel olarak çalıştırma

Önce PostgreSQL'i başlatın ve `src/main/resources/application.properties` içindeki bağlantı ayarlarının ortamınıza uygun olduğundan emin olun.

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

macOS/Linux:

```bash
./mvnw spring-boot:run
```

Uygulama varsayılan olarak `http://localhost:8080` adresinde çalışır.

## API Dokümantasyonu

Uygulama çalışırken Swagger UI üzerinden uç noktaları inceleyip istek gönderebilirsiniz:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Korumalı uç noktalar için önce giriş yaparak aldığınız JWT'yi Swagger'daki **Authorize** alanına `Bearer <token>` biçiminde girin.

## API Modülleri

| Modül | Kapsam |
| --- | --- |
| Authentication | Kayıt, giriş ve JWT oluşturma |
| Users | Kullanıcı ve rol yönetimi |
| Companies | Firma kayıtlarının yönetimi |
| Contacts | Firma bağlantılı kişi yönetimi |
| Leads | Aday toplama, durum güncelleme ve dönüştürme |
| Deals | Fırsat oluşturma, atama ve satış aşaması yönetimi |
| Activities | Arama, e-posta, toplantı vb. aktivite kayıtları |
| Reports | Pipeline özeti, dönüşüm, kazanılan satışlar ve performans raporları |

## Tipik Kullanım Akışı

1. Bir kullanıcı oluşturun ve giriş yaparak JWT alın.
2. Firma ve ilgili kişileri kaydedin.
3. Yeni lead'leri ekleyip kaynak ve durumlarını yönetin.
4. Nitelikli lead'leri fırsata dönüştürün.
5. Fırsatları satış aşamalarında ilerletin, sorumlu kullanıcı atayın ve aktiviteleri kaydedin.
6. Rapor uç noktalarından pipeline ve ekip performansını izleyin.

## Veritabanı Migrasyonları

Flyway, uygulama açılırken `src/main/resources/db/migration` altındaki SQL dosyalarını sürüm sırasıyla uygular. İlk kurulumda şema ve temel tablolar otomatik oluşturulur.

Yeni bir migrasyon eklerken aşağıdaki adlandırma düzenini kullanın:

```text
V7__aciklayici_migrasyon_adi.sql
```

## Testler

Tüm testleri çalıştırmak için:

Windows:

```powershell
.\mvnw.cmd test
```

macOS/Linux:

```bash
./mvnw test
```

## Ortam Yapılandırması

Veritabanı bağlantısı, JWT ayarları ve uygulama seçenekleri `src/main/resources/application.properties` üzerinden yapılandırılır. Üretim ortamında parolalar ve JWT gizli anahtarı gibi hassas değerleri kaynak koduna koymayın; ortam değişkenleri veya güvenli bir secret yönetim sistemi kullanın.

## Lisans

Bu proje için henüz bir lisans tanımlanmamıştır. Açık kaynak olarak yayımlamadan önce uygun bir lisans dosyası eklenmesi önerilir.
