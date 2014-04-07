Gem::Specification.new do |s|
  s.name               = "emailauth"
  s.version            = "0.0.4"

  s.add_runtime_dependency "rotp"

  s.default_executable = "emailauth"

  s.required_rubygems_version = Gem::Requirement.new(">= 0") if s.respond_to? :required_rubygems_version=
  s.authors = ["Brian Lubben"]
  s.date = %q{2014-03-27}
  s.description = %q{Email based authentication with mobile devices}
  s.email = %q{blubben@gmail.com}
  s.files = ["Rakefile", "lib/emailauth.rb", "lib/emailauth/authenticator.rb"]
  s.test_files = ["test/test_emailauth.rb"]
  s.require_paths = ["lib"]
  s.rubygems_version = %q{1.6.2}
  s.summary = %q{Hola Auth}

  if s.respond_to? :specification_version then
    s.specification_version = 3

    if Gem::Version.new(Gem::VERSION) >= Gem::Version.new('1.2.0') then
    else
    end
  else
  end
end
