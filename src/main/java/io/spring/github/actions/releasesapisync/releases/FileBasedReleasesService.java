/*
 * Copyright 2025-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.github.actions.releasesapisync.releases;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

class FileBasedReleasesService implements ReleasesService {

	private final Path filename;

	private final ObjectMapper mapper;

	FileBasedReleasesService(Path filename, ObjectMapper mapper) {
		this.filename = filename;
		this.mapper = mapper;
	}

	@Override
	public Collection<ReleaseRead> getReleases() {
		return getReleases(new TypeReference<>() {
		});
	}

	@Override
	public void putRelease(ReleaseWrite update) {
		Collection<ReleaseWrite> updates = new TreeSet<>();
		updates.add(update);
		updates.addAll(getReleases(new TypeReference<>() {
		}));
		updateFile(markCurrent(updates));
	}

	@Override
	public void deleteRelease(String version) {
		Collection<ReleaseWrite> updates = new TreeSet<>();
		Collection<ReleaseWrite> releases = getReleases(new TypeReference<>() {
		});
		releases.removeIf((r) -> r.version().equals(version));
		updateFile(markCurrent(updates));
	}

	private <T> Collection<T> getReleases(TypeReference<Collection<T>> ref) {
		try {
			String file = Files.readString(this.filename);
			return this.mapper.readValue(file, ref);
		}
		catch (IOException ex) {
			throw new RuntimeException("Unable to read release documentation links", ex);
		}
	}

	private static Collection<ReleaseRead> markCurrent(Collection<ReleaseWrite> updates) {
		Collection<ReleaseRead> marked = new ArrayList<>();
		boolean foundCurrent = false;
		for (ReleaseWrite release : updates) {
			if (release.status() != Status.GENERAL_AVAILABILITY) {
				marked.add(ReleaseRead.notAsCurrent(release));
				continue;
			}
			if (!foundCurrent) {
				marked.add(ReleaseRead.asCurrent(release));
				foundCurrent = true;
				continue;
			}
			marked.add(ReleaseRead.notAsCurrent(release));
		}
		return marked;
	}

	private void updateFile(Collection<ReleaseRead> releases) {
		try (OutputStream os = new FileOutputStream(this.filename.toFile())) {
			this.mapper.writeValue(os, releases);
		}
		catch (Exception ex) {
			throw new RuntimeException("Unable to update versions", ex);
		}
	}

}
