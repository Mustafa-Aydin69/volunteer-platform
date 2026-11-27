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
