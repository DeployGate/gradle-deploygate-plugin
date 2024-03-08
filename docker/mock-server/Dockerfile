FROM ruby:3.3.0 as dev

ENV RAILS_ENV development

RUN apt-get update -qq

WORKDIR /usr/src/app

COPY Gemfile /usr/src/app/Gemfile
COPY Gemfile.lock /usr/src/app/Gemfile.lock

RUN bundle install

COPY ./entrypoint.sh /usr/bin/
RUN chmod +x /usr/bin/entrypoint.sh

CMD ["/usr/bin/entrypoint.sh"]

FROM dev as ghcr

ADD . .

