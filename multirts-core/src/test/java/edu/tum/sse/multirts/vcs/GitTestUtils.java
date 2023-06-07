package edu.tum.sse.multirts.vcs;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

public final class GitTestUtils {
    public static RevCommit commit(Git repo) throws GitAPIException {
        return repo.commit().setAuthor("test", "test@test.org").setMessage("commit").setSign(false).call();
    }

    public static RevCommit commitEverything(Git repo) throws GitAPIException {
        repo.add().addFilepattern(".").call();
        return commit(repo);
    }

    public static void checkout(Git repo, String branch, boolean create) throws GitAPIException {
        if (create) repo.branchCreate().setName(branch).call();
        repo.checkout().setName(branch).call();
    }
}
