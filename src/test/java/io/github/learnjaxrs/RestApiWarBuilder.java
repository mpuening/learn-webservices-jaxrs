package io.github.learnjaxrs;

import java.io.PrintStream;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class RestApiWarBuilder {

	private WebArchive archive;

	public RestApiWarBuilder(String name) {
		this.archive = ShrinkWrap.create(WebArchive.class, name);
	}

	public RestApiWarBuilder packages(String... packages) {
		this.archive = archive.addPackages(true, packages);
		return this;
	}

	public RestApiWarBuilder beansXml() {
		this.archive = archive.addAsManifestResource(new ClassLoaderAsset("META-INF/beans.xml"), "beans.xml");
		return this;
	}

	public RestApiWarBuilder persistenceXml() {
		this.archive = archive.addAsManifestResource(new ClassLoaderAsset("META-INF/persistence.xml"), "persistence.xml");
		return this;
	}

	public RestApiWarBuilder indexHtml() {
		this.archive = archive.addAsWebInfResource(new ClassLoaderAsset("index.html"), "classes/index.html");
		return this;
	}

	public RestApiWarBuilder publicIndexHtml() {
		this.archive = archive.addAsWebInfResource(new ClassLoaderAsset("public/index.html"), "classes/public/index.html");
		return this;
	}

	public WebArchive build(boolean displayContents) {
		return displayContents ? build(System.out) : build();
	}

	public WebArchive build(PrintStream out) {
		out.println(archive.toString(true));
		return build();
	}

	public WebArchive build() {
		return archive;
	}
}
