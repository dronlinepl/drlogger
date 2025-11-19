# macOS Git Credential Helper - Usuwanie osxkeychain dla Jenkins

## Problem

Na macOS Git domyślnie używa `osxkeychain` credential helper. W środowisku Jenkins powoduje to błąd:

```
fatal: could not read Username for 'https://gitea.dr-online.local': Device not configured
```

Jenkins nie może przekazać credentials przez `osxkeychain`, ale może przez `cache` helper.

## Rozwiązanie

### 1. Sprawdź konfigurację

```bash
git config --list --show-origin | grep credential
```

### 2. NAJPIERW usuń linię z pliku systemowego

```bash
sudo nano /Library/Developer/CommandLineTools/usr/share/git-core/gitconfig
```

Znajdź i usuń całą linię:

```
helper = osxkeychain
```

### 3. Usuń z konfiguracji

```bash
git config --global --unset-all credential.helper
sudo git config --system --unset-all credential.helper
```

### 4. Ustaw cache helper

```bash
git config --global credential.helper cache
```

### 5. Sprawdź wynik

```bash
git config --list | grep credential
```

Powinno pokazać tylko:

```
credential.helper=cache
```

## Test w Jenkins

```groovy
sshagent(credentials: ['your-ssh-key-id']) {
    sh("git tag -a ${VTAG} -m 'Jenkins'")
    sh('git push origin --tags')
}
```

---

**Uwaga**: Kolejność jest krytyczna - najpierw plik, potem konfiguracja.