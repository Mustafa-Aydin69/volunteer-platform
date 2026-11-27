# Manuel Test Listesi

Bu dosya, MySQL ve IntelliJ olmayan bir bilgisayarda yazılıp yalnızca derlemesi
doğrulanan değişikliklerin, ana bilgisayarda sırayla yapılacak testlerini tutar.

Kurallar:
- Fazları **yukarıdan aşağı** sırayla test et. Bir test başarısız olursa orada dur,
  sonraki fazlara geçme; hatanın hangi commit'ten geldiği böylece belli olur.
- Geçen testin kutusunu `[x]` yap. Bir fazın tüm testleri geçince durumunu ✅ yap.
- Tüm fazlar geçince `feat/auth` branch'i `main`'e alınır.

---

## 0. İlk kurulum (ana bilgisayarda bir kez)

1. IntelliJ'de `D:\imece` (veya projenin olduğu klasör) açıkken sağ alttaki branch adına
   (`main` yazan yer) tıkla → **Fetch** simgesine (aşağı ok) bas → listede
   **Remote Branches > origin/feat/auth** üzerine gel → **Checkout**.
   - Doğrulama: sağ altta artık `feat/auth` yazmalı.
2. Sol Project panelinde `backend/pom.xml` dosyasına çift tıkla → editörün sağ üstünde
   beliren Maven simgesine (**Load Maven Changes**) tıkla. Simge yoksa sağ kenardaki
   **Maven** sekmesi → iki dairesel ok (**Reload All Maven Projects**).
   - Doğrulama: editörde kırmızı altı çizili `import` satırı kalmamalı.
3. Sağ üstteki listeden **Edit Configurations...** → `ImeceApplication-local` →
   **Environment variables** alanında şu dördünün olduğunu kontrol et:
   `DB_PASSWORD`, `JWT_SECRET`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`.
   - Doğrulama: alanda dört `AD=değer` çifti, aralarında `;` var.
4. MySQL80 servisinin çalıştığını kontrol et (MySQL Workbench bağlanabiliyorsa çalışıyor).

---

## 4b – Faz 1: Türkçe hata mesajları

- Commit: `5cfd6f0 chore(api): translate error messages to Turkish`
- Durum: ⏳ Bekliyor

### Testler
1. Yeşil ▶ ile `ImeceApplication-local`'ı çalıştır.
   - Beklenen: Run panelinde `Started ImeceApplication` satırı, kırmızı hata yok.
2. Tarayıcıda `http://localhost:8080/api/admin/dashboard` adresini aç.
   - Beklenen: `{"message":"Giriş yapmanız gerekiyor"}`
   - Türkçe karakterler düzgün olmalı (`GiriÅŸ` gibi bozuk görünmemeli).

### Sonuç
- [ ] Test 1
- [ ] Test 2

---

## 4b – Faz 2: Kayıt (register)

- Commit'ler (eskiden yeniye):
  - `d370119 feat(auth): add organization repository`
  - `dfee1c8 feat(auth): add user response DTO`
  - `81e3bd0 feat(auth): add auth response DTO`
  - `d50f43f feat(auth): add register request DTO with validation`
  - `3d5743a feat(auth): add registration logic to auth service`
  - `b905781 feat(auth): add register endpoint`
- Durum: ⏳ Bekliyor

### Hazırlık
1. Uygulama çalışıyor olsun (Faz 1, Test 1).
2. Başlat menüsünden **Windows PowerShell** aç ve aşağıdaki yardımcı fonksiyonu
   olduğu gibi yapıştırıp Enter'a bas. Hata yanıtlarında durum kodunu ve mesajı
   gösterir; Türkçe karakterleri UTF-8 olarak gönderir.
   ```powershell
   function Send-Register($body) {
       $json = [Text.Encoding]::UTF8.GetBytes(($body | ConvertTo-Json))
       try {
           $r = Invoke-RestMethod -Uri http://localhost:8080/api/auth/register -Method Post `
               -ContentType 'application/json; charset=utf-8' -Body $json
           "STATUS 201"; $r | ConvertTo-Json -Depth 5
       } catch {
           "STATUS " + [int]$_.Exception.Response.StatusCode; $_.ErrorDetails.Message
       }
   }
   ```
   - Beklenen: hiçbir çıktı yok (fonksiyon tanımlandı).

Not: Testler veritabanına kayıt ekler. Testleri ikinci kez çalıştırırsan 1. ve 2. test
409 döner; o zaman e-postaları değiştir (örn. `elif2@test.com`).

### Testler
1. **Gönüllü kaydı**
   ```powershell
   Send-Register @{ fullName='Elif Yılmaz'; email='elif@test.com'; password='sifre1234'; city='İstanbul'; role='VOLUNTEER' }
   ```
   - Beklenen: `STATUS 201`, `token` dolu, `user.role` = `VOLUNTEER`,
     `user.organizationStatus` = `null`, `İstanbul` düzgün.
2. **STK kaydı**
   ```powershell
   Send-Register @{ fullName='Ahmet Kaya'; email='yesiladim@test.com'; password='sifre1234'; city='İstanbul'; role='ORGANIZATION'; organizationName='Yeşil Adım Derneği'; organizationDescription='Çevre gönüllüleri' }
   ```
   - Beklenen: `STATUS 201`, `user.organizationStatus` = `PENDING`.
3. **Aynı e-posta (büyük harf ve boşlukla)**
   ```powershell
   Send-Register @{ fullName='Elif Y'; email='  ELIF@test.com '; password='sifre1234'; city='İzmir'; role='VOLUNTEER' }
   ```
   - Beklenen: `STATUS 409`, `{"message":"Bu e-posta zaten kayıtlı"}`
     (e-posta küçük harfe çevrilip boşlukları silindiği için aynı hesap sayılır).
4. **Admin olarak kayıt denemesi**
   ```powershell
   Send-Register @{ fullName='Kötü Niyet'; email='hacker@test.com'; password='sifre1234'; city='Ankara'; role='ADMIN' }
   ```
   - Beklenen: `STATUS 400`, `{"message":"Yönetici hesabı kayıtla oluşturulamaz"}`
5. **Kısa şifre (7 karakter)**
   ```powershell
   Send-Register @{ fullName='Ali Veli'; email='ali@test.com'; password='1234567'; city='Ankara'; role='VOLUNTEER' }
   ```
   - Beklenen: `STATUS 400`, `{"message":"password: Şifre 8 ile 72 karakter arasında olmalı"}`
6. **Kuruluş adı olmadan STK kaydı**
   ```powershell
   Send-Register @{ fullName='Ayşe Demir'; email='ayse@test.com'; password='sifre1234'; city='Bursa'; role='ORGANIZATION' }
   ```
   - Beklenen: `STATUS 400`, `{"message":"Kuruluş adı zorunludur"}`
7. **Veritabanı kontrolü** (MySQL Workbench'te çalıştır)
   ```sql
   SELECT id, email, role, LEFT(password_hash, 7) AS hash_start FROM imece.users;
   SELECT o.id, o.name, o.status, u.email FROM imece.organizations o JOIN imece.users u ON u.id = o.user_id;
   ```
   - Beklenen: `elif@test.com` ve `yesiladim@test.com` satırları var, `hash_start` = `$2a$10$`;
     `ali@test.com`, `ayse@test.com`, `hacker@test.com` **yok**.
     İkinci sorguda `Yeşil Adım Derneği` satırı, `status` = `PENDING`.

### Sonuç
- [ ] Test 1
- [ ] Test 2
- [ ] Test 3
- [ ] Test 4
- [ ] Test 5
- [ ] Test 6
- [ ] Test 7

---

## 4b – Faz 3: Giriş (login)

- Commit'ler (eskiden yeniye):
  - `319d897 feat(auth): add login request DTO`
  - `b77ea21 feat(auth): add login logic to auth service`
  - `e71ae28 feat(auth): add login endpoint`
- Durum: ⏳ Bekliyor
- Ön koşul: Faz 2 testleri yapılmış olmalı (`elif@test.com` ve `yesiladim@test.com` hesapları var).

### Hazırlık
Aynı PowerShell penceresinde şu iki fonksiyonu yapıştırıp Enter'a bas.
`Send-Login` başarılı girişte token'ı `$global:token` değişkenine saklar;
`Send-Get` o token ile korumalı bir adrese istek atar.
```powershell
function Send-Login($body) {
    $json = [Text.Encoding]::UTF8.GetBytes(($body | ConvertTo-Json))
    try {
        $r = Invoke-RestMethod -Uri http://localhost:8080/api/auth/login -Method Post `
            -ContentType 'application/json; charset=utf-8' -Body $json
        $global:token = $r.token
        "STATUS 200"; $r | ConvertTo-Json -Depth 5
    } catch {
        "STATUS " + [int]$_.Exception.Response.StatusCode; $_.ErrorDetails.Message
    }
}
function Send-Get($url) {
    try {
        $r = Invoke-RestMethod -Uri $url -Headers @{ Authorization = "Bearer $global:token" }
        "STATUS 200"; $r | ConvertTo-Json -Depth 5
    } catch {
        "STATUS " + [int]$_.Exception.Response.StatusCode; $_.ErrorDetails.Message
    }
}
```
- Beklenen: hiçbir çıktı yok.

### Testler
1. **Admin girişi** (`ADMIN_EMAIL` ve `ADMIN_PASSWORD` olarak IntelliJ'e ne yazdıysan onları kullan)
   ```powershell
   Send-Login @{ email='admin@imece.local'; password='ADMIN_SIFREN' }
   ```
   - Beklenen: `STATUS 200`, `token` dolu, `user.role` = `ADMIN`.
2. **Admin token'ı admin adresinde**
   ```powershell
   Send-Get 'http://localhost:8080/api/admin/dashboard'
   ```
   - Beklenen: `STATUS 404` (mesaj İngilizce olabilir: `No static resource ...`).
     404 doğru sonuç: güvenlik katmanı token'ı kabul edip isteği geçirdi, sadece bu
     adreste henüz bir controller yok. 401 veya 403 görürsen test başarısız.
3. **Gönüllü girişi, e-posta büyük harfle**
   ```powershell
   Send-Login @{ email='ELIF@TEST.COM'; password='sifre1234' }
   ```
   - Beklenen: `STATUS 200`, `user.email` = `elif@test.com`, `user.role` = `VOLUNTEER`.
4. **Gönüllü token'ı admin adresinde**
   ```powershell
   Send-Get 'http://localhost:8080/api/admin/dashboard'
   ```
   - Beklenen: `STATUS 403`, `{"message":"Bu işlem için yetkiniz yok"}`
5. **Yanlış şifre**
   ```powershell
   Send-Login @{ email='elif@test.com'; password='yanlis1234' }
   ```
   - Beklenen: `STATUS 401`, `{"message":"E-posta veya şifre hatalı"}`
6. **Kayıtlı olmayan e-posta**
   ```powershell
   Send-Login @{ email='yok@test.com'; password='sifre1234' }
   ```
   - Beklenen: 5. testle **birebir aynı** yanıt: `STATUS 401`, `{"message":"E-posta veya şifre hatalı"}`
7. **Onay bekleyen STK girişi**
   ```powershell
   Send-Login @{ email='yesiladim@test.com'; password='sifre1234' }
   ```
   - Beklenen: `STATUS 200`, `user.role` = `ORGANIZATION`, `user.organizationStatus` = `PENDING`.
8. **Boş şifre**
   ```powershell
   Send-Login @{ email='elif@test.com'; password='' }
   ```
   - Beklenen: `STATUS 400`, `{"message":"password: Şifre boş olamaz"}`
9. **Pasif hesap**
   - MySQL Workbench'te: `UPDATE imece.users SET enabled = 0 WHERE email = 'elif@test.com';`
   ```powershell
   Send-Login @{ email='elif@test.com'; password='sifre1234' }
   ```
   - Beklenen: `STATUS 403`, `{"message":"Hesabınız pasif durumda"}`
   - Bitince geri al: `UPDATE imece.users SET enabled = 1 WHERE email = 'elif@test.com';`
     (Workbench "safe update" uyarısı verirse: Edit > Preferences > SQL Editor >
     en alttaki **Safe Updates** kutusunu kaldır → OK → Query > Reconnect to Server.)

### Sonuç
- [ ] Test 1
- [ ] Test 2
- [ ] Test 3
- [ ] Test 4
- [ ] Test 5
- [ ] Test 6
- [ ] Test 7
- [ ] Test 8
- [ ] Test 9

---

## 4b – Faz 4: Giriş yapan kullanıcı (/me)

- Commit'ler (eskiden yeniye):
  - `27f07de refactor(auth): extract user response builder`
  - `add2c3a feat(auth): add current user lookup to auth service`
  - `862b1d6 feat(auth): add current user endpoint`
- Durum: ⏳ Bekliyor
- Ön koşul: Faz 3'teki `Send-Login` ve `Send-Get` fonksiyonları aynı PowerShell
  penceresinde tanımlı olmalı. Pencereyi kapattıysan Faz 3 "Hazırlık"ı tekrar yapıştır.

Not: `27f07de` yalnızca kodu yeniden düzenler, davranışı değiştirmez. Faz 3 testleri
bu commit'ten sonra da aynı sonucu vermeli.

### Testler
1. **Gönüllü token'ıyla /me**
   ```powershell
   Send-Login @{ email='elif@test.com'; password='sifre1234' }
   Send-Get 'http://localhost:8080/api/auth/me'
   ```
   - Beklenen (ikinci komut): `STATUS 200`, `email` = `elif@test.com`,
     `role` = `VOLUNTEER`, `organizationStatus` = `null`. Yanıtta `token` **yok**.
2. **STK token'ıyla /me**
   ```powershell
   Send-Login @{ email='yesiladim@test.com'; password='sifre1234' }
   Send-Get 'http://localhost:8080/api/auth/me'
   ```
   - Beklenen: `STATUS 200`, `role` = `ORGANIZATION`, `organizationStatus` = `PENDING`.
3. **Token olmadan**
   ```powershell
   $global:token = ''
   Send-Get 'http://localhost:8080/api/auth/me'
   ```
   - Beklenen: `STATUS 401`, `{"message":"Giriş yapmanız gerekiyor"}`
4. **Bozuk token**
   ```powershell
   $global:token = 'abc'
   Send-Get 'http://localhost:8080/api/auth/me'
   ```
   - Beklenen: `STATUS 401`, `{"message":"Giriş yapmanız gerekiyor"}`
5. **Pasife alınan kullanıcı anında kesilir**
   ```powershell
   Send-Login @{ email='elif@test.com'; password='sifre1234' }
   Send-Get 'http://localhost:8080/api/auth/me'
   ```
   - Beklenen: önce `STATUS 200`.
   - Şimdi MySQL Workbench'te: `UPDATE imece.users SET enabled = 0 WHERE email = 'elif@test.com';`
   - **Yeniden giriş yapmadan**, aynı token'la tekrar:
     ```powershell
     Send-Get 'http://localhost:8080/api/auth/me'
     ```
   - Beklenen: `STATUS 401`. Token'ın süresi dolmamış olsa bile erişim kesildi;
     çünkü filtre her istekte kullanıcıyı veritabanından yeniden okuyor.
   - Geri al: `UPDATE imece.users SET enabled = 1 WHERE email = 'elif@test.com';`

### Sonuç
- [ ] Test 1
- [ ] Test 2
- [ ] Test 3
- [ ] Test 4
- [ ] Test 5

---

## 4b – Faz 5: Swagger UI

- Commit'ler (eskiden yeniye):
  - `8a1051c build(api): add springdoc-openapi dependency`
  - `bfad16e feat(api): configure OpenAPI info and JWT authorization`
  - `7424bfd feat(security): allow anonymous access to Swagger UI`
  - `783b519 feat(api): make Swagger UI switchable by environment variable`
- Durum: ⏳ Bekliyor
- Ön koşul: `pom.xml` değişti. "0. İlk kurulum" 2. adımdaki **Load Maven Changes**'i
  bu commit'ten sonra tekrar yap (springdoc indirilecek).

Swagger çalışınca Faz 2–4 testlerini PowerShell yerine buradan da yapabilirsin:
endpoint'i aç → **Try it out** → gövdeyi doldur → **Execute**.

### Testler
1. **Sayfa açılıyor mu?** Uygulamayı çalıştır, tarayıcıda
   `http://localhost:8080/swagger-ui.html` adresini aç.
   - Beklenen: başlıkta **İmece API**, altında `auth-controller` grubu ve içinde
     `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me`.
     Sağ üstte yeşil **Authorize** butonu.
2. **Token olmadan /me**: `GET /api/auth/me` → **Try it out** → **Execute**.
   - Beklenen: Responses bölümünde `Code 401`, gövde `{"message":"Giriş yapmanız gerekiyor"}`.
3. **Login ile token al**: `POST /api/auth/login` → **Try it out** → gövdeyi şöyle yap:
   `{ "email": "elif@test.com", "password": "sifre1234" }` → **Execute**.
   - Beklenen: `Code 200`, yanıtta `token`. `token` değerini tırnaklar olmadan kopyala.
4. **Authorize**: sağ üstteki **Authorize** → **Value** kutusuna token'ı yapıştır
   (başına `Bearer` yazma, Swagger kendisi ekler) → **Authorize** → **Close**.
   - Beklenen: endpoint'lerin yanındaki kilit simgeleri kapalı hale gelir.
5. **Token ile /me**: `GET /api/auth/me` → **Execute**.
   - Beklenen: `Code 200`, `email` = `elif@test.com`.
6. **Kapatma ayarı**: IntelliJ'de **Edit Configurations...** → `ImeceApplication-local` →
   Environment variables sonuna `;SWAGGER_ENABLED=false` ekle → **Apply** → **OK** →
   uygulamayı yeniden başlat → `http://localhost:8080/swagger-ui.html` adresini yenile.
   - Beklenen: sayfa açılmaz (404). Sonra `;SWAGGER_ENABLED=false` kısmını sil,
     **Apply** → **OK**, uygulamayı yeniden başlat; sayfa tekrar açılmalı.

### Sonuç
- [ ] Test 1
- [ ] Test 2
- [ ] Test 3
- [ ] Test 4
- [ ] Test 5
- [ ] Test 6
