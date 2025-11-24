SafeHer_MobileAPP - Push instructions

Purpose

This document explains how to safely overwrite the remote branch on GitHub with your local branch while creating a timestamped backup of the remote branch first.

Files provided

- `overwrite_remote.sh` - an interactive zsh script that:
  - detects your current branch,
  - adds the provided remote (HTTPS or SSH),
  - fetches remote refs,
  - creates a timestamped remote backup branch (if a remote branch exists),
  - prompts for confirmation, and
  - force-pushes your local branch to the remote (uses --force-with-lease by default).

Quick run (macOS / zsh)

1. Inspect the script first: `less overwrite_remote.sh`
2. Make it executable: `chmod +x overwrite_remote.sh`
3. Run it: `./overwrite_remote.sh`

The script will prompt you to choose HTTPS or SSH remote URL. If you choose HTTPS, Git will ask for credentials (username and password/token) when pushing. If you choose SSH, ensure your SSH key is configured with GitHub.

Authentication guidance (do NOT paste tokens here)

- SSH (recommended):
  1. Generate a key if you don't have one: `ssh-keygen -t ed25519 -C "your-email@example.com"`
  2. Add the public key to GitHub > Settings > SSH and GPG keys.
  3. Test: `ssh -T git@github.com`

- HTTPS with Personal Access Token (PAT):
  1. Create a PAT in GitHub with `repo` scope.
  2. Configure the macOS credential helper to store the token securely: `git config --global credential.helper osxkeychain`
  3. On first push via HTTPS, use your GitHub username and the PAT as the password. The helper stores it in the macOS keychain.

Security note - PLEASE READ

You pasted a PAT in chat earlier. That token is sensitive and can be used to access your GitHub account. Immediately revoke that token in your GitHub settings and create a new one if needed. Do NOT paste tokens or passwords into chat or public places.

Safety notes

- The script creates a backup branch named `origin/<branch>-backup-YYYYMMDD-HHMMSS` before any destructive push. Confirm the backup exists on GitHub before doing anything else.
- `--force-with-lease` is used by default; it is safer than `--force` because it refuses to overwrite if someone else pushed new commits on the remote since your last fetch.
- Force-pushing rewrites remote history and will affect other collaborators. Only do this when you're sure.

Non-destructive alternative

If you prefer not to overwrite the remote, fetch and merge or rebase the remote branch locally and resolve conflicts, then push normally:

1. `git fetch origin --prune`
2. `git checkout -b remote-<branch> origin/<branch>` (inspect remote branch)
3. `git checkout <branch>`
4. `git merge origin/<branch>`  # or `git rebase origin/<branch>`
5. Resolve conflicts, `git add` and `git commit` or `git rebase --continue`
6. `git push origin <branch>`

Next steps for me

I cannot run git push on your behalf because I do not have access to your credentials and I will not use the PAT you pasted. Run `./overwrite_remote.sh` locally and follow prompts. If you want, tell me which authentication method you prefer and I can provide tailored command-by-command guidance.

If you'd like, I can also create a version of the script that uses unconditional `--force` instead of `--force-with-lease`.

