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
